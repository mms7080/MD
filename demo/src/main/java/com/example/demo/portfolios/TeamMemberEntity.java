package com.example.demo.portfolios;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_member")
public class TeamMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String teamName;
    private String name;      // 이름
    private String role;      // 역할 (ex: 팀장, 팀원 등)
    
    @ElementCollection
    @CollectionTable(name = "team_member_parts", joinColumns = @JoinColumn(name = "team_member_id"))
    @Column(name = "part")
    private List<String> parts; // 담당 파트들 (ex: 회원, 지도, 유기동물 등)

    // JPA를 위한 기본 생성자
    public TeamMemberEntity() {}

    public TeamMemberEntity(String teamName,String name, String role, List<String> parts) {
        this.teamName = teamName;
        this.name = name;
        this.role = role;
        this.parts = parts;
    }

    // getter
    public String getTeamName() { return teamName; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public List<String> getParts() { return parts; }
}
