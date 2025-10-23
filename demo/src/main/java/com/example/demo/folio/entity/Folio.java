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
            @Index(name = "idx_folio_status_created", columnList = "FOLIO_STATUS, created_at") // ✅ DB 스키마에 맞춤
        })
public class Folio {

    @Id
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Users user;

    // ✅ DB에 컬럼이 없으므로 영속 제외(INSERT/SELECT/UPDATE에 포함되지 않음)
    @Transient
    private String title = "Untitled";

    @Column(length = 50, nullable = false)
    private String template = "dev-basic";

    public enum Status { DRAFT, PUBLISHED }

    // ✅ 실제 컬럼명과 일치
    @Enumerated(EnumType.STRING)
    @Column(name = "FOLIO_STATUS", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    @Lob
    @Column(name = "content_json", columnDefinition = "CLOB", nullable = false)
    private String contentJson = "{}";

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill", length = 100)
    @BatchSize(size = 50)
    private List<String> skills = new ArrayList<>();

    @Lob
    @Column(name = "introduction", columnDefinition = "CLOB")
    private String introduction;

    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_photos", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "photo_url", length = 500, nullable = false)
    @OrderColumn(name = "ordinal")
    @BatchSize(size = 50)
    private List<String> photos = new ArrayList<>();

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
        if ((this.thumbnail == null || this.thumbnail.isBlank())
                && this.photos != null && !this.photos.isEmpty()) {
            this.thumbnail = this.photos.get(0);
        }
    }
}
