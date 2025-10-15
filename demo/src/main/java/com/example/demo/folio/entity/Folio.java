package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "folio")
public class Folio {
    
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // ── 신규: 어떤 템플릿으로 만든 건지 (예: "dev-basic")
    @Column(length = 50, nullable = false)
    private String template = "dev-basic";

    // ── 신규: DRAFT / PUBLISHED
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.DRAFT;

    // ── 신규: PPT 편집기의 전체 state JSON (프론트 state 그대로)
    // PostgreSQL이면 jsonb, 그 외 DB면 TEXT/CLOB 권장
    @Lob
    @Column(name = "data_json", columnDefinition = "CLOB", nullable = false)
    private String data;  // 프론트 state JSON

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "CLOB")
    private String introduction;

    private String thumbnail;

    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_photos", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_projects", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "project_id")
    private List<String> projectIds = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updateAt;

    public enum Status {DRAFT, PUBLISHED}
    
}