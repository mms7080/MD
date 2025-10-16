package com.example.demo.portfolios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.dto.TeamMemberDto;
import com.example.demo.portfolios.entity.PortfolioLikeEntity;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.portfolios.repository.PortfolioLikeRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfoliosRepository repository;
    private final PortfolioCommentRepository commentRepository;
    private final PortfolioLikeRepository likeRepository;
    private final UsersRepository usersRepository;

    /**
     * ✅ 리스트 페이지용 — LazyInitialization 방지용 트랜잭션 유지
     */
    @Transactional(readOnly = true)
    public List<PortfoliosEntity> getAllPortfolios(org.springframework.data.domain.Pageable pageable) {
        return repository.findAllBasic(pageable).getContent(); // ✅ EntityGraph로 미리 로드됨
    }

    /**
     * 파일 저장 (이미지/ZIP 구분)
     */
    public String saveFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) return null;

        long maxSize = type.equals("zip") ? 50 * 1024 * 1024 : 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IOException("파일 용량 초과: " + file.getOriginalFilename());
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

        if (type.equals("image")) {
            if (!(extension.equals("jpg") || extension.equals("jpeg")
                    || extension.equals("png") || extension.equals("webp"))) {
                throw new IOException("이미지 파일만 업로드 가능: " + originalName);
            }
        } else if (type.equals("zip")) {
            if (!extension.equals("zip")) {
                throw new IOException("ZIP 파일만 업로드 가능: " + originalName);
            }
        }

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + filename;
    }

    /**
     * DB 저장 메서드
     */
    @Transactional
    public void saveFromDto(PortfolioFormDto dto, String coverPath, String iconPath, String downloadPath) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<String> screenshotPaths = dto.getScreenshots() != null
            ? dto.getScreenshots().stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    try {
                        return saveFile(file, "image");
                    } catch (IOException e) {
                        throw new RuntimeException("스크린샷 저장 실패", e);
                    }
                })
                .toList()
            : java.util.Collections.emptyList();

        PortfoliosEntity entity = PortfoliosEntity.builder()
            .title(dto.getTitle())
            .creator(currentUser)
            .tags(dto.getTags())
            .cover(coverPath)
            .desc(dto.getDesc())
            .screenshots(screenshotPaths)
            .icon(iconPath)
            .link(dto.getLink())
            .download(downloadPath)
            .likes(new LinkedHashSet<>())
            .createdAt(LocalDateTime.now())
            .teamName(dto.getTeamName())
            .build();

        var team = dto.getTeam().stream()
            .map(t -> {
                TeamMemberEntity member = new TeamMemberEntity(
                        t.getMemberName(),
                        t.getMemberRole(),
                        t.getParts()
                );
                member.setPortfolio(entity);
                return member;
            })
            .toList();

        entity.setTeam(team);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public PortfoliosEntity getPortfolioWithTeam(Long id) {
        return repository.findDetailById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));
    }

    @Transactional
    public void deletePortfolio(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 존재하지 않습니다. id=" + id));

        commentRepository.deleteByPortfolioId(id);
        repository.delete(portfolio);
    }

    @Transactional
    public PortfoliosEntity increaseViewCount(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

        if (portfolio.getViewCount() == null) {
            portfolio.setViewCount(0);
        }

        portfolio.setViewCount(portfolio.getViewCount() + 1);
        return repository.save(portfolio);
    }

    @Transactional
    public void updatePortfolio(Long id, PortfolioFormDto dto) throws IOException {
        // ✅ 수정 전용 쿼리 (댓글/좋아요는 미포함)
        PortfoliosEntity portfolio = repository.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));
    
        // ✅ 기본 텍스트 필드
        portfolio.setTitle(dto.getTitle());
        portfolio.setDesc(dto.getDesc());
        portfolio.setLink(dto.getLink());
        portfolio.setTeamName(dto.getTeamName());
    
        /* ---------------------------
            ✅ 파일 관련 항목 (빈 값일 경우 기존 유지)
        --------------------------- */
    
        // 대표 이미지
        if (dto.getCover() != null && !dto.getCover().isEmpty()) {
            // 새 이미지 업로드한 경우
            portfolio.setCover(saveFile(dto.getCover(), "image"));
        } else {
            // 기존 이미지 유지
            portfolio.setCover(portfolio.getCover());
        }
    
        // 아이콘
        if (dto.getIcon() != null && !dto.getIcon().isEmpty()) {
            portfolio.setIcon(saveFile(dto.getIcon(), "image"));
        } else {
            portfolio.setIcon(portfolio.getIcon());
        }
    
        // ZIP 파일
        if (dto.getDownload() != null && !dto.getDownload().isEmpty()) {
            portfolio.setDownload(saveFile(dto.getDownload(), "zip"));
        } else {
            portfolio.setDownload(portfolio.getDownload());
        }
    
        // ✅ 스크린샷 (비었으면 기존 이미지 유지)
        if (dto.getScreenshots() != null && !dto.getScreenshots().isEmpty()) {
            List<String> screenshotPaths = new ArrayList<>();
            for (MultipartFile file : dto.getScreenshots()) {
                if (!file.isEmpty()) {
                    screenshotPaths.add(saveFile(file, "image"));
                }
            }
            portfolio.setScreenshots(screenshotPaths);
        } else {
            portfolio.setScreenshots(portfolio.getScreenshots());
        }
    
            // ✅ 태그 병합 로직
    if (dto.getTags() != null && !dto.getTags().isEmpty()) {
        // 기존 태그 가져오기 (null-safe)
        Set<String> existingTags = portfolio.getTags() != null
                ? new LinkedHashSet<>(portfolio.getTags())
                : new LinkedHashSet<>();

        // 새로 입력된 태그 합치기
        existingTags.addAll(dto.getTags());

        // 병합 결과 다시 세팅
        portfolio.setTags(existingTags);
    }
    
        /* ---------------------------
            ✅ 팀원 갱신
        --------------------------- */
        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {
            List<TeamMemberEntity> existing = portfolio.getTeam();
            existing.clear();
    
            for (TeamMemberDto t : dto.getTeam()) {
                TeamMemberEntity member = new TeamMemberEntity();
                member.setMemberName(t.getMemberName());
                member.setMemberRole(t.getMemberRole());
                member.setParts(t.getParts());
                member.setPortfolio(portfolio);
                existing.add(member);
            }
        } else {
            portfolio.setTeam(portfolio.getTeam()); // 기존 팀 유지
        }
    
        repository.save(portfolio);
    }
    


@Transactional(readOnly = true)
public PortfolioFormDto getPortfolioForm(Long id) {
    // ✅ 기존: findDetailById() → comments, screenshots 둘 다 fetch
    // ❌ 변경: 수정용으로 fetch 줄이기
    PortfoliosEntity entity = repository.findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));

    return PortfolioFormDto.formEntityDto(entity);
}


    @Transactional
    public int toggleLike(Long id, Principal principal) {
        Users user = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        PortfoliosEntity portfolio = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));
    
        Optional<PortfolioLikeEntity> existingLike = likeRepository.findByPortfolioAndUser(portfolio, user);
    
        if (existingLike.isPresent()) {
            // ✅ 이미 좋아요 눌렀으면 취소
            likeRepository.delete(existingLike.get());
        } else {
            // ✅ 좋아요 추가
            PortfolioLikeEntity like = new PortfolioLikeEntity();
            like.setPortfolio(portfolio);
            like.setUser(user);
            likeRepository.save(like);
        }
    
        // ✅ 즉시 반영된 좋아요 개수 반환
        return likeRepository.countByPortfolio(portfolio);
    }
    


    @Transactional(readOnly = true)
public PortfoliosEntity getPortfolioDetail(Long id) {
    PortfoliosEntity portfolio = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

    // ✅ Lazy 초기화 강제 (트랜잭션 내에서 미리 로드)
    portfolio.getScreenshots().size();
    portfolio.getTeam().size();
    portfolio.getTags().size();
    portfolio.getLikes().size();
    portfolio.getComments().size();

    return portfolio;
}
}
