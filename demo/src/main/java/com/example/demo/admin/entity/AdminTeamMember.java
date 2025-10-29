package com.example.demo.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "AdminTeamMember")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "admin_team_members")
public class AdminTeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private AdminTeam team; // ✅ mappedBy="team" 과 매칭

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;
}
