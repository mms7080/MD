package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "folio",
        indexes = {
            @Index(name = "idx_folio_user", columnList = "user_id"),
            @Index(name = "idx_folio_status_created", columnList = "status, created_at")
        })
public class Folio {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Users user;

    // 목록/상세 표시에 사용 (필수 권장)
    @Column(length = 200, nullable = false)
    private String title = "Untitled";

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
    @Column(name = "skill", length = 100)
    @BatchSize(size = 50)
    private List<String> skills = new ArrayList<>();

    @Lob
    @Column(name = "introduction", columnDefinition = "CLOB")
    private String introduction;

    // 썸네일(명시적으로 지정하지 않으면 첫 사진 사용)
    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    // 사진(세로 슬라이드 순서 보장)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_photos", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "photo_url", length = 500, nullable = false)
    @OrderColumn(name = "ordinal")
    @BatchSize(size = 50)
    private List<String> photos = new ArrayList<>();

    // 연계 프로젝트(필요 시)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_projects", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "project_id", length = 100)
    @BatchSize(size = 50)
    private List<String> projectIds = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void ensureThumbnail() {
        if ((this.thumbnail == null || this.thumbnail.isBlank()) && this.photos != null && !this.photos.isEmpty()) {
            this.thumbnail = this.photos.get(0);
        }
    }
}

