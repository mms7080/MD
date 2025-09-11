package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FolioDetailDto {
    private String folioId;
    private UserInFolioDto user;
    private List<String> skills;

    // API 명세에 맞게 projects 필드를 추가합니다.
    private List<PortfolioInFolioDto> projects;
    private String introduction;
    // API 명세에 맞게 photos 필드를 추가합니다.
    private List<String> photos;

     // Entity를 DTO로 변환하는 생성자
     public FolioDetailDto(Folio folio){
        this.folioId = folio.getId();
        this.user = new UserInFolioDto(folio.getUser());
        this.skills = folio.getSkills();
        this.introduction = folio.getIntroduction();
        
        // 지금은 포트폴리오와 사진 데이터가 없으므로 임시 데이터를 넣습니다.
        this.projects = List.of(
            new PortfolioInFolioDto("1", "Pawple - 반려동물 건강 관리"),
            new PortfolioInFolioDto("2", "Filmora - 영화 예매 사이트")
        );
        this.photos = List.of(
            "https://picsum.photos/seed/photo1/800/600",
            "https://picsum.photos/seed/photo2/800/600"
        );
     }
}
