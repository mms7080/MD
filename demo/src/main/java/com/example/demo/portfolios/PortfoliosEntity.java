
package com.example.demo.portfolios;

import java.util.List;

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
    

    // 생성자에 포함
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

    // Getter (Thymeleaf는 getter 메서드 기준으로 속성을 찾습니다)
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreator() { return creator; }
    public List<String> getTags() { return tags; }
    public int getLikes() { return likes; }
    public String getCreatedAt() { return createdAt; }
    public String getCover() { return cover; }
    public String getDesc() { return desc; }
    public List<String> getScreenshots() { return screenshots; }
    public List<TeamMemberEntity> getTeam() { return team; }
    public String getIcon() { return icon; }
    public String getLink() { return link; }
    public String getDownload() { return download; }
}
