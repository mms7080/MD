package com.example.demo.notice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTICE")
@Getter
@Setter
@NoArgsConstructor      // 기본 생성자
@AllArgsConstructor     // 모든 필드 생성자
@ToString               // 디버깅 시 편리
@Builder                // 빌더 패턴 자동 생성
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq")
    @SequenceGenerator(
        name = "notice_seq",
        sequenceName = "NOTICE_SEQ", // 오라클에 직접 생성됨
        allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, length = 255)
    private String title; // 제목

    @Lob
    @Column(nullable = false)
    private String content; // 내용 (CLOB 매핑됨)

    @Column(nullable = false, length = 100)
    private String writer; // 작성자

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 작성일

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
