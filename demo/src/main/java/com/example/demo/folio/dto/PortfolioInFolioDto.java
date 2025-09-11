package com.example.demo.folio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Folio 상세정보에 포함될 포트폴리오 카드 정보
@Getter
@AllArgsConstructor
public class PortfolioInFolioDto {
    private String portfolioId;
    private String title;
}
