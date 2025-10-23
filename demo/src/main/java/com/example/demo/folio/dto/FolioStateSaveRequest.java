package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;

import lombok.Getter;
import lombok.Setter;


/** PPT 에디터 저장/불러오기용: state 전체 JSON을 그대로 주고받음 */
@Getter
@Setter
public class FolioStateSaveRequest {
    private String template;      // "dev-basic"
    private String contentJson;   // 프론트 state JSON.stringify
    private Folio.Status status;  // optional: DRAFT/PUBLISHED
    private String thumbnail;     // optional
}
