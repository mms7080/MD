package com.example.demo.portfolios.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfoliosRepository repository;

    public PortfoliosEntity save(PortfoliosEntity portfolios){
        return repository.save(portfolios);
    }

    public void saveFromDto(PortfolioFormDto dto) throws IOException {
    String currentUser = SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getName(); // 로그인 사용자 이름/ID

    // 1️⃣ 업로드 경로 설정
    String uploadDir = "uploads/";
    Files.createDirectories(Paths.get(uploadDir));

    // 2️⃣ cover 파일 처리
    String coverPath = null;
    if (dto.getCover() != null && !dto.getCover().isEmpty()) {
        String filename = UUID.randomUUID() + "_" + dto.getCover().getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        dto.getCover().transferTo(filePath.toFile());
        coverPath = "/uploads/" + filename; // DB에는 경로 저장
    }

    // 3️⃣ icon 파일 처리
    String iconPath = null;
    if (dto.getIcon() != null && !dto.getIcon().isEmpty()) {
        String filename = UUID.randomUUID() + "_" + dto.getIcon().getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        dto.getIcon().transferTo(filePath.toFile());
        iconPath = "/uploads/" + filename;
    }

    // 4️⃣ download ZIP 처리
    String downloadPath = null;
    if (dto.getDownload() != null && !dto.getDownload().isEmpty()) {
        String filename = UUID.randomUUID() + "_" + dto.getDownload().getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        dto.getDownload().transferTo(filePath.toFile());
        downloadPath = "/uploads/" + filename;
    }

    // 5️⃣ DTO → Entity 변환
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


}
