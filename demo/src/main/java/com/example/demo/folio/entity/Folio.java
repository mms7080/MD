package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "template", length = 50, nullable = false)
    private String template = "dev-basic";

    public enum Status { DRAFT, PUBLISHED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "title", length = 200, nullable = false)
    private String title = "Untitled";

    /** ✅ CLOB 로 *명확히* 바인딩 */
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "content_json", columnDefinition = "CLOB")
    private String contentJson;

    @Column(name = "thumbnail", length = 1000)
    private String thumbnail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    /** ✅ 이것도 CLOB 로 명확히 */
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "introduction", columnDefinition = "CLOB")
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

    @PrePersist
    protected void prePersist() {
        if (template == null || template.isBlank()) template = "dev-basic";
        if (status == null) status = Status.DRAFT;
        if (title == null || title.isBlank()) title = "Untitled";
        if (contentJson == null) contentJson = "{}";
    }

    public void setSkills(List<String> skills) {
        this.skills = (skills == null) ? new ArrayList<>() : new ArrayList<>(skills);
    }
    public void setPhotos(List<String> photos) {
        this.photos = (photos == null) ? new ArrayList<>() : new ArrayList<>(photos);
    }
    public void setProjectIds(List<String> projectIds) {
        this.projectIds = (projectIds == null) ? new ArrayList<>() : new ArrayList<>(projectIds);
    }
}
