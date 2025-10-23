package com.example.demo.portfolios.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.portfolios.service.CommentService;
import com.example.demo.portfolios.service.PortfolioService;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

@Controller
@RequestMapping("/portfolios")
public class PortfoliosController {

    private final PortfolioService portfolioService;
    private final PortfoliosRepository portfoliosRepository;
    private final CommentService commentService;
    private final PortfolioCommentRepository commentRepository;
    private final UsersRepository usersRepository;

    public PortfoliosController(
            PortfolioService portfolioService,
            PortfoliosRepository portfoliosRepository,
            CommentService commentService,
            PortfolioCommentRepository commentRepository,
            UsersRepository usersRepository
    ) {
        this.portfolioService = portfolioService;
        this.portfoliosRepository = portfoliosRepository;
        this.commentService = commentService;
        this.commentRepository = commentRepository;
        this.usersRepository = usersRepository;
    }

    /**
     * ✅ 포트폴리오 목록 조회 (페이징 + Lazy 방지)
     */
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 🔥 Service를 통해 가져오기 (트랜잭션 유지됨)
        List<PortfoliosEntity> portfolios = portfolioService.getAllPortfolios(pageable);

        System.out.println("✅ Portfolio count: " + portfolios.size());
        portfolios.forEach(p -> System.out.println(" - " + p.getTitle()));

        model.addAttribute("portfolios", portfolios);
        return "portfolios/list";
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public String getPortfolio(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               Model model,
                               Principal principal) {
        try {
            // ✅ 조회수 증가
            portfolioService.increaseViewCount(id);
    
            // ✅ 포트폴리오 상세 조회
            PortfoliosEntity portfolio = portfolioService.getPortfolioDetail(id);
    
            // ✅ 비공개 처리
            if (Boolean.FALSE.equals(portfolio.getIsPublic())) {
                boolean isAdmin = false;
                if (principal != null) {
                    Users user = usersRepository.findByUsername(principal.getName()).orElse(null);
                    if (user != null && user.getRole() == Role.ADMIN) {
                        isAdmin = true;
                    }
                }
                if (!isAdmin) {
                    System.out.println("🚫 비공개 포트폴리오 접근 차단 (USER)");
                    model.addAttribute("notFound", true);
                    return "portfolios/detail";
                }
            }
    
            // ✅ 댓글, 별점 정보
            int size = 5;
            Page<PortfolioComment> commentPage = commentService.getComments(id, page, size);
            double avgRating = commentService.getAverageRating(id);
            long ratingCount = commentService.getCommentCount(id);
    
            // ✅ 모델 데이터 추가
            model.addAttribute("portfolio", portfolio);
            model.addAttribute("comments", commentPage.getContent());
            model.addAttribute("tags", portfolio.getTags());
            model.addAttribute("screenshots", portfolio.getScreenshots());
            model.addAttribute("avgRating", avgRating);
            model.addAttribute("ratingCount", ratingCount);
            model.addAttribute("currentPage", commentPage.getNumber());
            model.addAttribute("totalPages", commentPage.getTotalPages());
    
            // ✅ 디버그 로그
            System.out.println("🟢 포트폴리오 접근 성공: " + portfolio.getTitle());
            System.out.println(" - tags: " + portfolio.getTags());
            System.out.println(" - comments: " + (portfolio.getComments() != null ? portfolio.getComments().size() : "null"));
            System.out.println(" - screenshots: " + (portfolio.getScreenshots() != null ? portfolio.getScreenshots().size() : "null"));
    
            return "portfolios/detail";
    
        } catch (AccessDeniedException e) {
            // ✅ 비공개 접근 거부
            System.err.println("🚫 AccessDeniedException: 비공개 포트폴리오 접근 시도 - " + e.getMessage());
            model.addAttribute("notFound", true);
            return "portfolios/detail";
    
        } catch (org.hibernate.LazyInitializationException e) {
            // ✅ Lazy 로딩 실패 (세션 닫힘)
            System.err.println("⚠️ LazyInitializationException 발생: " + e.getMessage());
            model.addAttribute("errorMsg", "데이터를 불러오는 중 문제가 발생했습니다. (Lazy)");
            return "portfolios/detail";
    
        } catch (Exception e) {
            // ✅ 그 외 모든 예외
            System.err.println("❌ 예외 발생: " + e.getClass().getSimpleName());
            e.printStackTrace();
            model.addAttribute("errorMsg", "포트폴리오를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("notFound", true);
            return "portfolios/detail";
        }
    }
    

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("portfolioFormDto", new PortfolioFormDto());
        return "portfolios/create";
    }

    @PostMapping
    public String create(@ModelAttribute PortfolioFormDto dto) throws IOException {
        String coverPath = portfolioService.saveFile(dto.getCover(), "image");
        String iconPath = portfolioService.saveFile(dto.getIcon(), "image");
        String downloadPath = portfolioService.saveFile(dto.getDownload(), "zip");
        portfolioService.saveFromDto(dto, coverPath, iconPath, downloadPath);
        return "redirect:/portfolios";
    }

    @PostMapping("/delete/{id}")
    public String deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return "redirect:/portfolios";
    }

    @PostMapping({"/{id}/comments"})
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @RequestParam int rating,
                             Principal principal) {
        commentService.addComment(id, content, rating, principal);
        return "redirect:/portfolios/" + id;
    }

    @PostMapping("/{portfolioId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long portfolioId,
                                @PathVariable Long commentId,
                                Principal principal) {
        commentService.deleteComment(portfolioId, commentId, principal);
        return "redirect:/portfolios/" + portfolioId;
    }

    @GetMapping("/{portfolioId}/comments/{commentId}/edit")
    public String editCommentForm(@PathVariable Long portfolioId,
                                  @PathVariable Long commentId,
                                  Principal principal,
                                  Model model) {
        PortfolioComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        Users currentUser = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isOwner = comment.getUser().getUsername().equals(currentUser.getUsername());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        model.addAttribute("comment", comment);
        model.addAttribute("portfolioId", portfolioId);
        return "portfolios/comment-edit";
    }

    @PostMapping("/{portfolioId}/comments/{commentId}/edit")
    public String editComment(@PathVariable Long portfolioId,
                              @PathVariable Long commentId,
                              @RequestParam String content,
                              @RequestParam int rating,
                              Principal principal) {
        commentService.updateComment(portfolioId, commentId, content, rating, principal);
        return "redirect:/portfolios/" + portfolioId;
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<Integer> increaseViews(@PathVariable Long id) {
        PortfoliosEntity updated = portfolioService.increaseViewCount(id);
        return ResponseEntity.ok(updated.getViewCount());
    }

    @GetMapping("/edit/{id}")
    public String editPortfolioForm(@PathVariable Long id, Model model) {
        PortfolioFormDto dto = portfolioService.getPortfolioForm(id);
        model.addAttribute("portfolioFormDto", dto);
        model.addAttribute("portfolioId", id);
        return "portfolios/edit";
    }

    @PostMapping("/{id}/edit")
public String updatePortfolio(@PathVariable Long id,
                              @ModelAttribute PortfolioFormDto dto) {
    try {
        portfolioService.updatePortfolio(id, dto);
    } 
    catch (org.apache.tomcat.util.http.fileupload.FileUploadException e) {
        // Tomcat 업로드 예외 (개수 초과 등)
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, 
            "업로드 파일 개수 제한을 초과했습니다. (최대 20개까지 가능)"
        );
    } 
    catch (IOException e) {
        // 파일 저장 오류 등
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "파일 처리 중 오류가 발생했습니다."
        );
    }

    return "redirect:/portfolios/" + id;
}

@PostMapping("/{id}/like")
@ResponseBody
public ResponseEntity<String> toggleLike(@PathVariable Long id, Principal principal) {
    if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
    }
    try {
        int count = portfolioService.toggleLike(id, principal);
        // ✅ String 으로 명시적 변환, Spring이 text/plain 자동 설정
        return ResponseEntity.ok(String.valueOf(count));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("error");
    }
}




}
