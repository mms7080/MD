package com.example.demo.portfolios.dto;

import java.util.List;

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
    private List<String> tags;
    private MultipartFile cover;
    private String desc;
    private List<String> screenshot;
    private List<TeamMemberDto> team;
    private String coverPath;
    private String iconPath;
    private String downloadPath;
    private MultipartFile icon;
    private String link;
    private MultipartFile download;

    public static PortfolioFormDto formEntityDto(PortfoliosEntity entity){
        return PortfolioFormDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .tags(entity.getTags())
            .coverPath(entity.getCover())
            .desc(entity.getDesc())
            .screenshot(entity.getScreenshots())
            .team(entity.getTeam().stream()
                .map(t -> new TeamMemberDto(
                        t.getTeamName(),
                        t.getMemberName(),
                        t.getMemberRole(),
                        t.getParts()))
                .toList())
            .iconPath(entity.getIcon())          // 경로 저장
            .downloadPath(entity.getDownload())  // 경로 저장
            .link(entity.getLink())
            .build();
    }
    

}
