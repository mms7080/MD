package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Collections; // Collections 임포트

@Getter
public class FoliosSummaryDto {
    private String folioId;
    private String userName;
    private String thumbnail;
    private List<String> skills;

    // 🔹 추가
    private String status;                 // DRAFT / PUBLISHED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String title;                  // (선택) 필요한 곳에서 쓸 수 있게

    public FoliosSummaryDto(Folio folio){
        this.folioId = folio.getId();
        this.userName = folio.getUser().getName();
        this.thumbnail = folio.getThumbnail();
        this.skills = (folio.getSkills() != null) ? folio.getSkills() : Collections.emptyList();

        // 🔹 추가 매핑
        this.status = folio.getStatus() != null ? folio.getStatus().name() : null;
        this.createdAt = folio.getCreatedAt();
        this.updatedAt = folio.getUpdatedAt();
        this.title = folio.getTitle();
    }
}