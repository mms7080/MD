
package com.example.demo.portfolios;

import java.util.List;

import lombok.Getter;

@Getter
// 임시 entity
public class PortfoliosEntity {
    private String id;
    private String title;
    private String creator;
    private List<String> tags;
    private int likes;
    private String createdAt;
    private String cover;
    private String desc;

    // 추가
    private List<String> screenshots; 
    private List<TeamMemberEntity> team;

    private String icon; // 팀 아이콘
    private String link; // 실제 접속 경로
    private String download; // 코드 다운로드 폴더 
    

    // 생성자
    public PortfoliosEntity(String id, String title, String creator, List<String> tags, int likes,
                   String createdAt, String cover, String desc, List<String> screenshots,List<TeamMemberEntity> team
                   ,String icon, String link, String download) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.tags = tags;
        this.likes = likes;
        this.createdAt = createdAt;
        this.cover = cover;
        this.desc = desc;
        this.screenshots = screenshots;
        this.team = team;
        this.icon = icon;
        this.link = link;
        this.download = download;
    }

    

    public static void put(String string, PortfoliosEntity portfoliosEntity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }
}
