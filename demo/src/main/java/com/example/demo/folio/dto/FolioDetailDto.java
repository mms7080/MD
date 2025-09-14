package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Getter
public class FolioDetailDto {
    private String folioId;
    private UserInFolioDto user;
    private List<String> skills;
    private List<PortfolioInFolioDto> projects;
    private String introduction;
    private List<String> photos;
    private String thumbnail;

    public FolioDetailDto(Folio folio) {
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.skills = folio.getSkills();
        this.introduction = folio.getIntroduction();
        this.thumbnail = folio.getThumbnail();
        
        this.projects = new ArrayList<>(); 
        this.photos = new ArrayList<>();
     }
}
