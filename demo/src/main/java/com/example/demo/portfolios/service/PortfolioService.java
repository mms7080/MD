package com.example.demo.portfolios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.dto.TeamMemberDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfoliosRepository repository;
    private final PortfolioCommentRepository commentRepository;
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

        // ✅ 프로젝트 내부 static/uploads/ 경로
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명에 UUID 붙이기
        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // ✅ DB에는 /uploads/filename 으로 저장
        return "/uploads/" + filename;
    }

    /**
     * DB 저장 메서드
     */
    @Transactional
    public void saveFromDto(PortfolioFormDto dto, String coverPath, String iconPath, String downloadPath) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // ✅ 스크린샷 파일 저장 (MultipartFile → String 경로 리스트)
        List<String> screenshotPaths = dto.getScreenshots() != null
            ? dto.getScreenshots().stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    try {
                        return saveFile(file, "image"); // 파일 저장 후 경로 반환
                    } catch (IOException e) {
                        throw new RuntimeException("스크린샷 저장 실패", e);
                    }
                })
                .toList()
            : java.util.Collections.emptyList();

        // ✅ 부모 엔티티 생성
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
            .likes(0)
            .createdAt(LocalDateTime.now())
            .teamName(dto.getTeamName()) // ✅ 팀명은 부모에 한 번만 저장
            .build();

        // ✅ 팀원 리스트 (teamName 제거됨)
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
        return repository.findDetailByIdWithTeam(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));
    }

    // 삭제
    @Transactional
    public void deletePortfolio(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 존재하지 않습니다. id=" + id));

            commentRepository.deleteByPortfolioId(id);

        repository.delete(portfolio);  // ✅ Cascade 때문에 팀원/태그/스크린샷도 자동 삭제
    }


    @Transactional
public PortfoliosEntity increaseViewCount(Long id) {
    PortfoliosEntity portfolio = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

    // ✅ null 방어
    if (portfolio.getViewCount() == null) {
        portfolio.setViewCount(0);
    }

    portfolio.setViewCount(portfolio.getViewCount() + 1);
    return repository.save(portfolio);
}


// 수정
// 수정 처리
@Transactional
public void updatePortfolio(Long id, PortfolioFormDto dto) throws IOException {
    PortfoliosEntity portfolio = getPortfolioWithTeam(id);

    // ===== 일반 필드 수정 =====
    portfolio.setTitle(dto.getTitle());
    portfolio.setDesc(dto.getDesc());
    portfolio.setLink(dto.getLink());

    // ===== 파일 수정 =====
    if (dto.getCover() != null && !dto.getCover().isEmpty()) {
        String coverPath = saveFile(dto.getCover(), "image");
        portfolio.setCover(coverPath);
    }
    if (dto.getIcon() != null && !dto.getIcon().isEmpty()) {
        String iconPath = saveFile(dto.getIcon(), "image");
        portfolio.setIcon(iconPath);
    }
    if (dto.getDownload() != null && !dto.getDownload().isEmpty()) {
        String downloadPath = saveFile(dto.getDownload(), "zip");
        portfolio.setDownload(downloadPath);
    }

    // ===== 태그 수정 =====
    if (dto.getTags() != null) {
        portfolio.setTags(dto.getTags());
    }

    // ===== 팀원 수정 =====
    List<TeamMemberEntity> existing = portfolio.getTeam();

    // 1) 기존 컬렉션 비우기
    existing.clear();

    // 2) DTO 기반으로 다시 채우기
    for (TeamMemberDto t : dto.getTeam()) {
        TeamMemberEntity member;

        if (t.getId() != null) {
            // 기존 멤버를 찾을 필요가 없음 → 어차피 clear() 했으니까 새로 채워야 함
            member = new TeamMemberEntity();
            member.setId(t.getId()); // 필요하다면 유지
        } else {
            member = new TeamMemberEntity();
        }

        member.setMemberName(t.getMemberName());
        member.setMemberRole(t.getMemberRole());
        member.setParts(t.getParts());
        member.setPortfolio(portfolio); // FK 연결
        existing.add(member);
    }

    // repository.save(portfolio) 는 @Transactional 이라면 없어도 flush 됨
    repository.save(portfolio);
}


@Transactional(readOnly = true)
public PortfolioFormDto getPortfolioForm(Long id) {
    PortfoliosEntity portfolio = repository.findDetailById(id)
        .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음: " + id));
    return PortfolioFormDto.formEntityDto(portfolio); // 세션이 살아있으니 LAZY 초기화됨
}


// //  좋아요
// @Transactional
// public int likePortfolio(Long id) {
//     PortfoliosEntity portfolio = repository.findById(id)
//         .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));
//     portfolio.setLikes(portfolio.getLikes() + 1); // 👍 JPA가 dirty checking으로 update
//     return portfolio.getLikes();
// }

// @Transactional
// public int unlikePortfolio(Long id) {
//     PortfoliosEntity portfolio = repository.findById(id)
//         .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));
//     portfolio.setLikes(Math.max(0, portfolio.getLikes() - 1));
//     return portfolio.getLikes();
// }


    
}













