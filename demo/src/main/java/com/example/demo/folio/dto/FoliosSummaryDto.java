package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;
import java.util.List;
import java.util.Collections; // Collections 임포트

@Getter
public class FoliosSummaryDto {
    private String folioId;
    private String userName;
    private String thumbnail;
    private List<String> skills;

    public FoliosSummaryDto(Folio folio){
        this.folioId = folio.getId();
        this.userName = folio.getUser().getName();
        this.thumbnail = folio.getThumbnail();
        // --- 수정된 부분: skills가 null일 경우 빈 리스트로 초기화 ---
        this.skills = (folio.getSkills() != null) ? folio.getSkills() : Collections.emptyList();
       
    }
}