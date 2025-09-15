package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.ArrayList; 

@Getter
@Setter
@Entity
public class Folio {
    
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

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
    
}