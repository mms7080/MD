package com.example.demo.users.UsersEntity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING) // ✅ enum을 문자열로 저장
    @Column(nullable = false, length = 10)
    private Role role = Role.USER; // ✅ 기본값 USER

    @Column(nullable = false, length = 10)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "delete_status", nullable = false, length = 1)
    private DeleteStatus deleteStatus = DeleteStatus.N; // 탈퇴 여부 

    @Column
    private LocalDateTime deletedAt; // 삭제일자

}