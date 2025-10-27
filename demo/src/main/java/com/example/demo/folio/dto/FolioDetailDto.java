package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class FolioDetailDto {
    private String folioId;
    private UserInFolioDto user;
    private String introduction;
    private List<String> skills;
    private List<String> photos;
    private List<PortfolioInFolioDto> projects;

    // ✅ SSR에서 바로 쓰게 파싱된 상태를 담아줌
    private Map<String, Object> state;

    @JsonProperty("id")
    public String getId() { return folioId; }

    public FolioDetailDto(Folio folio, List<PortfolioInFolioDto> projects, Map<String,Object> state) {
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.introduction = folio.getIntroduction();
        this.skills = folio.getSkills();
        this.photos = folio.getPhotos();
        this.projects = projects != null ? projects : List.of();
        this.state = state != null ? state : Map.of();
    }

    public FolioDetailDto(Folio folio, Map<String,Object> state) {
        this(folio, List.of(), state);
    }
}
