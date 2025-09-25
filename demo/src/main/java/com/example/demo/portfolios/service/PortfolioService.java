package com.example.demo.portfolios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
// import com.example.demo.portfolios.entity.PortfolioLikesEntity;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
// import com.example.demo.portfolios.repository.PortfolioLikeRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfoliosRepository repository;
    private final UsersRepository userRepository;
    // private final PortfolioLikeRepository likeRepository;

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
    
        // ✅ 앞에 "/" 추가 (절대 경로로 저장)
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
        .createdAt(LocalDateTime.now())
        .teamName(dto.getTeamName()) // ✅ 팀명은 부모에 한 번만 저장
        .build();
    
                // ✅ 팀원 리스트 (teamName 제거됨)
                var team = dto.getTeam().stream()
                .map(t -> {
                    String role = t.getMemberRole(); // DTO에서 가져옴
            
                    TeamMemberEntity member = new TeamMemberEntity(
                        t.getMemberName(),
                        (role == null || role.trim().isEmpty()) ? null : role, // ✅ null 처리
                        t.getParts()
                    );
            
                    member.setPortfolio(entity);
                    return member;
                })
                .toList();

        entity.setTeam(team);

        repository.save(entity);
    }
    
    


    public PortfoliosEntity getPortfolioById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));
    }

    // 삭제
    @Transactional
    public void deletePortfolio(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 존재하지 않습니다. id=" + id));

        repository.delete(portfolio);  // ✅ Cascade 때문에 팀원/태그/스크린샷도 자동 삭제
    }




//     @Transactional
// public void likePortfolio(Long portfolioId, String username) {
//     PortfoliosEntity portfolio = repository.findById(portfolioId)
//         .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));

//     Users user = userRepository.findByUsername(username)
//         .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

//     // 이미 좋아요 눌렀는지 확인
//     boolean exists = likeRepository.existsByPortfolioAndUser(portfolio, user);
//     if (exists) {
//         throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
//     }

//     PortfolioLikesEntity like = new PortfolioLikesEntity();
//     like.setPortfolio(portfolio);
//     like.setUser(user);

//     likeRepository.save(like);
// }

// @Transactional
// public void unlikePortfolio(Long portfolioId, String username) {
//     PortfoliosEntity portfolio = repository.findById(portfolioId)
//         .orElseThrow(() -> new IllegalArgumentException("포트폴리오 없음"));

//     Users user = userRepository.findByUsername(username)
//         .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

//     likeRepository.deleteByPortfolioAndUser(portfolio, user);
// }



}

