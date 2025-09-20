package com.example.demo.users.UsersEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="user_profile")
public class Profile {
    @Id
    private Long id;

    @Column
    private String profileImgUrl;

    @Column
    private String githubUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_profile_position", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "position")
    private Set<String> positions  = new HashSet<>();
    
}
