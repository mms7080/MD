package com.example.demo.portfolios;

import java.util.List;

public class TeamMemberEntity {
    private String teamName;
    private String name;      // 이름
    private String role;      // 역할 (ex: 팀장, 팀원 등)
    private List<String> parts; // 담당 파트들 (ex: 회원, 지도, 유기동물 등)

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
