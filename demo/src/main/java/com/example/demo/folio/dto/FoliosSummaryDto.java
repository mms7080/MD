package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FoliosSummaryDto {
    private final String id;
    private final String title;
    private final String template;
    private final String status;        // "DRAFT" | "PUBLISHED"
    private final LocalDateTime updatedAt;
    private final String thumbnail;

    public static FoliosSummaryDto from(Folio f) {
        String title = (f.getTitle() != null && !f.getTitle().isBlank())
                ? f.getTitle().trim()
                : "제목 없음";
        return new FoliosSummaryDto(
                f.getId(),
                title,
                f.getTemplate(),
                f.getStatus().name(),
                f.getUpdatedAt(),
                f.getThumbnail()
        );
    }
}
