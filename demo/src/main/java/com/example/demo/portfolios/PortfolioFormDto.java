package com.example.demo.portfolios;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioFormDto {
    
    private String id;
    private String title;
    private String creator;
    private List<String> tags;
    private String cover;
    private String desc;
    private List<String> screenshot;
    private List<TeamMemberDto> team;
    private String icon;
    private String link;
    private String download;

    // Entity -> Dto 전환 메서드
    public static PortfolioFormDto formEntityDto(PortfoliosEntity entity){
        return PortfolioFormDto.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .creator(entity.getCreator())
            .tags(entity.getTags())
            .cover(entity.getCover())
            .desc(entity.getDesc())
            .screenshot(entity.getScreenshots())
            .team(entity.getTeam().stream()
                .map(t-> new TeamMemberDto(t.getTeamName(), t.getName(),t.getRole()))
                .toList())
            .icon(entity.getIcon())
            .link(entity.getLink())
            .download(entity.getDownload())
            .build();
    }

}
