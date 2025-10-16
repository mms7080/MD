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

    @Column(length = 50, nullable = false)
    private String template = "dev-basic";

    public enum Status { DRAFT, PUBLISHED }

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.DRAFT;

    // PPT 에디터 전체 상태
    @Lob
    @Column(name = "content_json", nullable = false)
    private String contentJson;

    // 아래 메타/보조 필드들은 유지해도 됨(목록/검색용)
    private String thumbnail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Lob
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
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt; 
}
