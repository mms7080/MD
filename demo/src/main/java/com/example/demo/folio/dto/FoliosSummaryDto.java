package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;
import java.util.List;

@Getter
public class FoliosSummaryDto {
    private String  folioId;
    private UserInFolioDto user;
    private List<String> skills;
    private String thumbnail;

    public FoliosSummaryDto(Folio folio){
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.skills = folio.getSkills();
        this.thumbnail = folio.getThumbnail();
    }
}
