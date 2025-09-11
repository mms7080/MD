package com.example.demo.notice;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq")
    @SequenceGenerator(
        name = "notice_seq",
        sequenceName = "NOTICE_SEQ", 
        allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, length = 255)
    private String title; // 제목

    @Lob
    @Column(nullable = false)
    private String content; // 내용 

    @Column(nullable = false, length = 100)
    private String writer; // 작성자

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 작성일

    @Column(name = "view_count", nullable = false)
    private int views = 0; // 조회수 

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //getter/setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
}
