package com.example.demo.folio.dto;

import com.example.demo.folio.entity.Folio;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FolioRequestDto {
    /** 목록/상세 표시에 사용될 제목 */
    private String title;

    /** 소개/요약(선택) */
    private String introduction;

    /** 태그/기술 스택(선택) */
    private List<String> skills;

    /** 업로드된 사진 URL 목록(순서 중요) */
    private List<String> photos;

    /** 연계할 포트폴리오(다른 도메인) ID 목록(선택) */
    private List<String> projectIds;

    /** 명시적 썸네일(없으면 첫 사진으로 자동 처리) */
    private String thumbnail;

    /** 템플릿 키(없으면 dev-basic) */
    private String template;

    /** 에디터 전체 상태 JSON(없으면 "{}") */
    private String contentJson;

    /** 상태(DRAFT/PUBLISHED). 없으면 엔티티 기본값 DRAFT 사용 */
    private Folio.Status status;
}
