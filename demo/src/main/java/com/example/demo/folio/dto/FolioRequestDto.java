package com.example.demo.folio.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolioRequestDto {
    private String introduction;
    private List<String> skills;
    private List<String> photos; // 업로드된 사진 URL 목록
    private List<String> projectIds; // 선택된 포트폴리오 ID 목록
}
