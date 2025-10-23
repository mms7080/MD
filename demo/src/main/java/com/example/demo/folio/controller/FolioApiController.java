package com.example.demo.folio.controller;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.service.FolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/folios")
@RequiredArgsConstructor
public class FolioApiController {

    private final FolioService folioService;

    /** 공개 목록: PUBLISHED만, 최신순 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFoliosPage(
            @RequestParam(defaultValue = "1") int page,   // 프론트 1-base
            @RequestParam(defaultValue = "12") int size
    ) {
        int zeroBased = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<FoliosSummaryDto> folioPage = folioService.getFolioSummaries(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("page", folioPage.getNumber() + 1);
        response.put("items", folioPage.getContent());
        response.put("totalPages", folioPage.getTotalPages());
        response.put("totalItems", folioPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /** 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<FolioDetailDto> getFolioDetail(@PathVariable String id) {
        try {
            FolioDetailDto detail = folioService.getFolioDetail(id);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 생성(또는 새 버전 생성) – 기본 DRAFT, 제목/썸네일/템플릿/JSON/상태까지 반영 가능 */
    @PostMapping
    public ResponseEntity<FolioDetailDto> createOrUpdateFolio(
            @RequestBody FolioRequestDto requestDto,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        Folio saved = folioService.createOrUpdateFolio(requestDto, principal);
        return ResponseEntity.ok(new FolioDetailDto(saved));
    }

    /** 에디터 전체 상태 저장(템플릿별 최신본 유지) – DRAFT↔PUBLISHED 전환 가능 */
    @PostMapping("/dev-basic")
    public ResponseEntity<FolioDetailDto> saveDevBasic(
            @RequestBody FolioStateSaveRequest req,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        Folio saved = folioService.saveState(principal, req);
        return ResponseEntity.ok(new FolioDetailDto(saved));
    }

    /** 로그인 사용자의 최신 저장본(템플릿별) */
    @GetMapping("/me/{template}")
    public ResponseEntity<Map<String, Object>> loadMyFolio(
            @PathVariable String template, Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        return folioService.getMyLatest(principal, template)
                .map(f -> {
                    Map<String,Object> body = new HashMap<>();
                    body.put("id", f.getId());
                    body.put("template", f.getTemplate());
                    body.put("status", f.getStatus());
                    body.put("thumbnail", f.getThumbnail());
                    body.put("contentJson", f.getContentJson());
                    body.put("updatedAt", f.getUpdatedAt());
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
