
package com.example.demo.portfolios;

import java.util.List;

import jakarta.persistence.CascadeType;
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
    private String id;
    
    private String title;
    private String creator;

    @ElementCollection
    private List<String> tags;
    private int likes;
    private String createdAt;
    private String cover;
    private String desc;

    // 추가
    @ElementCollection
    private List<String> screenshots; 

    @OneToMany(cascade = CascadeType.ALL)
    private List<TeamMemberEntity> team;

    private String icon; // 팀 아이콘
    private String link; // 실제 접속 경로
    private String download; // 코드 다운로드 폴더 
    

    // 생성자
    public PortfoliosEntity(String title, String creator, List<String> tags, int likes,
                   String createdAt, String cover, String desc, List<String> screenshots,List<TeamMemberEntity> team
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
