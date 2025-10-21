package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    // 어떤 템플릿으로 만든 건지
    @Column(length = 50, nullable = false)
    private String template = "dev-basic";

    // 상태
    public enum Status { DRAFT, PUBLISHED }
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.DRAFT;

    // 전체 에디터 상태 JSON
    @Lob
    @Column(name = "content_json", columnDefinition = "CLOB", nullable = false)
    private String contentJson = "{}";

    // 요약 정보들 (있으면 사용)
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
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt; 
}

