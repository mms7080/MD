package com.example.demo.folio.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersService.UsersService;

import java.security.Principal;

import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.folio.service.FolioService; // FolioService 주입



@Controller
@RequestMapping("/folios") // 이 컨트롤러의 모든 주소는 /folios로 시작
@RequiredArgsConstructor
public class FolioController {

    private final UsersService usersService;
    private final FolioService folioService; 
    private final FolioRepository folioRepository;

    // 1. 목록 페이지 뷰
    @GetMapping
    public String folioListPage(Model model){
        return "folios/list";
    }

    // 2. 상세 페이지 뷰
    @GetMapping("/detail/{id}")
    public String folioDetailPage(@PathVariable String id, Model model, Principal principal) {
        // 엔티티(photos 포함)를 직접 로드
        Folio entity = folioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 Folio"));

        boolean isOwner = principal != null
                && entity.getUser() != null
                && principal.getName().equals(entity.getUser().getUsername());

        // 공개 정책: 발행본 공개, DRAFT는 오너만
        if (!(entity.getStatus() == Folio.Status.PUBLISHED || isOwner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // (선택) LAZY 안전하게 초기화 보증
        entity.getPhotos().size();
        System.out.println("[FolioDetail] photos.size=" + (entity.getPhotos() == null ? "null" : entity.getPhotos().size()));

        // 부가 정보가 필요하면 DTO도 같이
        var detail = folioService.getFolioDetail(id);

        model.addAttribute("folio", entity);   // ✅ 템플릿은 이걸 사용
        model.addAttribute("detail", detail);  // 프로젝트/상태 등 부가정보
        model.addAttribute("isOwner", isOwner);
        return "folios/detail";
    }

    // 3. 작성 페이지 뷰
    @GetMapping("/write")
    public String folioWritePage() {
        return "folios/write";
    }

    // 4. 관리자용 Append 페이지 뷰
    @GetMapping("/append")
    public String folioAdminAppendPage() {
        return "folios/append";
    }

    // 5. 편집 페이지 뷰
    @GetMapping("/edit")
    public String folioEditPage(
            @RequestParam(name = "template", required = false, defaultValue = "dev-basic") String template,
            Model model,
            Principal principal
    ) {
        if (principal != null) {
            Users currentUser = usersService.getUserByUsername(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("template", template); // edit.html에서 ${template} 사용
        return "folios/edit";
    }


}