package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList; // ArrayList 임포트

@Getter
public class FolioDetailDto {
    private String folioId;
    private UserInFolioDto user;
    private String introduction;
    private List<String> skills;
    private List<String> photos;
    private List<PortfolioInFolioDto> projects;

    // 1. 상세 정보 조회 시 사용 (포트폴리오 정보 포함)
    public FolioDetailDto(Folio folio, List<PortfolioInFolioDto> projects) {
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.introduction = folio.getIntroduction();
        this.skills = folio.getSkills();
        this.photos = folio.getPhotos();
        this.projects = projects;
    }

    // 2. 저장 직후 응답 시 사용 (포트폴리오 정보 미포함)
    public FolioDetailDto(Folio folio) {
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.introduction = folio.getIntroduction();
        this.skills = folio.getSkills();
        this.photos = folio.getPhotos();
        this.projects = new ArrayList<>(); // 빈 리스트로 초기화
    }
}