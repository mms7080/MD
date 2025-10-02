package com.example.demo.portfolios.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="portfolio")
@SequenceGenerator(name="portfolio_seq_gen", sequenceName="portfolio_seq", allocationSize=1)
public class PortfoliosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portfolio_seq_gen")
    private Long id;

    @Column(name="p_title", nullable = false)
    private String title;

    @Column(name="p_creator" ,nullable = false)
    private String creator;

    // ✅ 태그 (Set)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "portfolio_tags",
        joinColumns = @JoinColumn(name = "portfolio_id")
    )
    @Column(name = "tag", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name="p_likes")
    private Integer likes = 0;

    @Column(name="p_createdAt")
    private LocalDateTime createdAt;

    @Column(name="p_cover")
    private String cover;

    @Column(name="p_desc", length=2000)
    private String desc;

    // ✅ 스크린샷 (입력 순서 유지 → List + OrderColumn)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "portfolio_screenshots",
        joinColumns = @JoinColumn(name = "portfolio_id")
    )
    @Column(name = "screenshot")
    @Fetch(FetchMode.SUBSELECT) // ← ✅ 추가!
    private List<String> screenshots = new ArrayList<>();

    @Column(name="p_teamName")
    private String teamName;

    // ✅ 팀원 (입력 순서 유지 → List + OrderColumn)
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name="team_order")
    @Fetch(FetchMode.SUBSELECT)
    private List<TeamMemberEntity> team = new ArrayList<>();

    @Column(name="p_icon")
    private String icon;

    @Column(name="p_link")
    private String link;

    @Column(name="p_download")
    private String download;

    @Column(name="p_views")
    private  Integer viewCount = 0;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioComment> comments = new ArrayList<>();
}
