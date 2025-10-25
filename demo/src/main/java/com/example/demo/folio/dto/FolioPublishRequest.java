package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class FolioPublishRequest {
    private String template;          // "dev-basic"
    private String title;             // 카드/상세에 쓸 제목
    private String contentJson;       // 전체 상태 JSON
    private Folio.Status status;      // PUBLISHED (혹은 DRAFT 재활용 가능)
    private String thumbnail;         // 선택 (없으면 첫 슬라이드)
    private List<String> images;      // 슬라이드 이미지를 Base64 dataURL로 보냄 (html2canvas 결과)
}