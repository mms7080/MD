package com.example.demo.portfolios.dto;

import java.util.List;
import java.util.Set;

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
    
    private Long id;
    private String title;
    private Set<String> tags;

    // 파일 업로드
    private MultipartFile cover;
    private List<MultipartFile> screenshots; // ✅ 이름 통일 (복수형)

    // DB에 저장된 경로들
    private String coverPath;
    private List<String> screenshotPaths; // ✅ 출력용 (entity.getScreenshots())

    private String desc;
    private List<TeamMemberDto> team;

    private String iconPath;
    private MultipartFile icon;
    private String downloadPath;
    private MultipartFile download;
    private String link;

    public static PortfolioFormDto formEntityDto(PortfoliosEntity entity){
        return PortfolioFormDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .tags(entity.getTags())
            .coverPath(entity.getCover())
            .desc(entity.getDesc())
            .screenshotPaths(entity.getScreenshots()) // ✅ 여기로 매핑
            .team(entity.getTeam().stream()
                .map(t -> new TeamMemberDto(
                        t.getTeamName(),
                        t.getMemberName(),
                        t.getMemberRole(),
                        t.getParts()))
                .toList())
            .iconPath(entity.getIcon())
            .downloadPath(entity.getDownload())
            .link(entity.getLink())
            .build();
    }
}

