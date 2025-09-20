
package com.example.demo.portfolios.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
// 임시 entity
@Entity
@Table(name="portfolio")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfoliosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)    
    private Long id;

    @Column(name="p_title")
    private String title;
    @Column(name="p_creator")
    private String creator;

    @ElementCollection
    @Column(name="p_tags")
    private List<String> tags;
    @Column(name="p_likes")
    private int likes;
    @Column(name="p_createdAt")
    private LocalDateTime createdAt;
    @Column(name="p_cover")
    private String cover;
    @Column(name="p_desc")
    private String desc;

    // 추가
    @ElementCollection
    @Column(name="p_screenshots")
    private List<String> screenshots; 

    @OneToMany(cascade = CascadeType.ALL)
    @Column(name="team")
    private List<TeamMemberEntity> team;

    @Column(name="p_icon")
    private String icon; // 팀 아이콘
    @Column(name="p_link")
    private String link; // 실제 접속 경로
    @Column(name="p_download")
    private String download; // 코드 다운로드 폴더 
    

    // 생성자
    public PortfoliosEntity(String title, String creator, List<String> tags, int likes,
                   LocalDateTime createdAt, String cover, String desc, List<String> screenshots,List<TeamMemberEntity> team
                   ,String icon, String link, String download) {
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
