package com.example.demo.portfolios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfoliosRepository repository;

    /**
     * 파일 저장 (이미지/ZIP 구분)
     */
    public String saveFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 용량 제한
        long maxSize = type.equals("zip") ? 50 * 1024 * 1024 : 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IOException("파일 용량 초과: " + file.getOriginalFilename());
        }

        // 확장자 체크
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

        // 저장 경로 생성
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // UUID 붙여 저장
        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + filename; // DB에 저장되는 상대경로
    }

    /**
     * DB 저장 메서드
     */
    @Transactional
    public void saveFromDto(PortfolioFormDto dto, String coverPath, String iconPath, String downloadPath) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        PortfoliosEntity entity = PortfoliosEntity.builder()
                .title(dto.getTitle())
                .creator(currentUser)
                .tags(dto.getTags())
                .cover(coverPath)
                .desc(dto.getDesc())
                .screenshots(dto.getScreenshot())
                .team(dto.getTeam().stream()
                        .map(t -> new TeamMemberEntity(
                                t.getTeamName(),
                                t.getMemberName(),
                                t.getMemberRole(),
                                t.getParts()))
                        .toList())
                .icon(iconPath)
                .link(dto.getLink())
                .download(downloadPath)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(entity);
    }
    
    public PortfoliosEntity getPortfolioById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));
    }
    
}

