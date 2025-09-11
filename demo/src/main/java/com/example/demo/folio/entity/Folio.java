package com.example.demo.folio.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "folio_skills", joinColumns = @JoinColumn(name = "folio_id"))
    @Column(name = "skill")
    private List<String> skills;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    private String thumbnail;
}
