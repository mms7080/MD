package com.example.demo.portfolios.entity;

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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_member")
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="teamName")
    private String teamName;
    @Column(name="memberName")
    private String memberName;
    @Column(name="memberRole")      // 이름
    private String memberRole;      // 역할 (ex: 팀장, 팀원 등)
    
    @ElementCollection
    @CollectionTable(name = "team_member_parts", joinColumns = @JoinColumn(name = "team_member_id"))
    @Column(name = "part")
    private List<String> parts; // 담당 파트들 (ex: 회원, 지도, 유기동물 등)


    public TeamMemberEntity(String teamName,String memberName, String memberRole, List<String> parts) {
        this.teamName = teamName;
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.parts = parts;
    }

    // getter
    public String getTeamName() { return teamName; }
    public String getMemberName() { return memberName; }
    public String getMemberRole() { return memberRole; }
    public List<String> getParts() { return parts; }
}
