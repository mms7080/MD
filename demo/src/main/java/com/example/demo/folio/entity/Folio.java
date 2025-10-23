package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

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

    /** 템플릿 식별자 (예: "dev-basic") */
    @Column(name = "template", length = 50, nullable = false)
    private String template = "dev-basic";

    public enum Status { DRAFT, PUBLISHED }

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    /** 목록/상세 표기용 제목 */
    @Column(name = "title", length = 200, nullable = false)
    private String title = "Untitled";

    /** 에디터 전체 상태 JSON */
    @Lob
    @Column(name = "content_json", nullable = false)
    private String contentJson = "{}";

    /** 목록 썸네일 URL */
    @Column(name = "thumbnail")
    private String thumbnail;

    /** (선택) 보조 메타들 — 기존 유지 */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Lob
    @Column(name = "introduction")
    private String introduction;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_photos", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_projects", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "project_id")
    private List<String> projectIds = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 널 방어 – DB 기본값이 아닌 JPA 필드 기본값이 적용되도록 보강 */
    @PrePersist
    protected void prePersist() {
        if (template == null || template.isBlank()) template = "dev-basic";
        if (status == null) status = Status.DRAFT;
        if (title == null || title.isBlank()) title = "Untitled";
        if (contentJson == null) contentJson = "{}";
    }
}
