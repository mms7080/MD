package com.example.demo.portfolios.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.entity.PortfoliosEntity;

import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioFormDto {
    
    private String title;
    private Set<String> tags;

    // 파일 업로드
    private MultipartFile cover;
    private List<MultipartFile> screenshots; // ✅ 이름 통일 (복수형)

    // DB에 저장된 경로들
    private String coverPath;
    private List<String> screenshotPaths = new ArrayList<>(); // ✅ 출력용 (entity.getScreenshots())

    private String desc;
    private String teamName;
    private List<TeamMemberDto> team;

    private String iconPath;
    private MultipartFile icon;
    private String downloadPath;
    private MultipartFile download;
    private String link;
    private Integer viewCount;

    public static PortfolioFormDto formEntityDto(PortfoliosEntity entity){
        return PortfolioFormDto.builder()
            .title(entity.getTitle())
            .tags(entity.getTags() != null 
                ? entity.getTags()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                : Set.of())
            .coverPath(entity.getCover())
            .desc(entity.getDesc())
            .screenshotPaths(
                entity.getScreenshots() != null
                    ? new ArrayList<>(
                        new LinkedHashSet<>(
                            entity.getScreenshots().stream()
                                .filter(Objects::nonNull)   // null 제거
                                .map(String::valueOf)       // String 보장
                                .toList()
                        )
                    )
                    : new ArrayList<>()
            )
            .team(entity.getTeam() != null ? entity.getTeam().stream()
                .map(t -> new TeamMemberDto(
                        t.getId(),
                        t.getMemberName(),
                        t.getMemberRole(),
                        t.getParts()))
                .toList() : new ArrayList<>()) // ✅ null-safe
            .iconPath(entity.getIcon())
            .downloadPath(entity.getDownload())
            .link(entity.getLink())
            .viewCount(entity.getViewCount() != null ? entity.getViewCount() : 0) // ✅ null-safe
            .build();
    }
    
}

