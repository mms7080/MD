package com.example.demo.folio.controller;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FolioPublishRequest;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.service.FolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;
import com.example.demo.folio.entity.Folio;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;



@RestController
@RequestMapping("/api/folios") // API 경로를 명세서에 맞게 /api/folios로 변경
@RequiredArgsConstructor
public class FolioApiController {

    private final FolioService folioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFoliosPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 최신순 정렬(ID 기준 내림차순)을 적용한 Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        Page<FoliosSummaryDto> folioPage = folioService.getFolioSummaries(pageable);

        // API 응답 형식에 맞게 Map을 구성
        Map<String, Object> response = new HashMap<>();
        response.put("page", folioPage.getNumber() + 1); // 프론트엔드는 1부터 시작
        response.put("items", folioPage.getContent());
        response.put("totalPages", folioPage.getTotalPages());
        response.put("totalItems", folioPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolioDetailDto> getFolioDetail(@PathVariable String id) {
        try{
            FolioDetailDto folioDetail = folioService.getFolioDetail(id);
            return ResponseEntity.ok(folioDetail);
        } catch (IllegalArgumentException e) {
            // 해당 ID의 Folio가 없을 경우 404 Not Found 응답을 보냅니다.
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FolioDetailDto> createOrUpdateFolio(
            @RequestBody FolioRequestDto requestDto,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        
        Folio savedFolio = folioService.createOrUpdateFolio(requestDto, principal);
        
        FolioDetailDto responseDto = new FolioDetailDto(savedFolio, Map.of());
        

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/dev-basic")
    public ResponseEntity<Map<String, Object>> saveDevBasic(
            @RequestBody FolioStateSaveRequest req,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        Folio saved = folioService.saveState(principal, req);

        // 응답 최소화: LAZY 초기화 이슈 방지
        Map<String, Object> body = new HashMap<>();
        body.put("id", saved.getId());

        return ResponseEntity.ok(body);
    }

    @PostMapping("/dev-basic/publish")
    public ResponseEntity<Map<String, Object>> publishDevBasic(
            @RequestBody FolioPublishRequest req, Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        var saved = folioService.publishAsImages(principal, req); // 새 서비스 메서드
        return ResponseEntity.ok(Map.of("id", saved.getId()));
    }

    // ① 에디터 불러오기(내 dev-basic 최신 상태) — 프런트 edit.js가 호출 중
    @GetMapping("/me/dev-basic")
    public ResponseEntity<?> getMyDevBasic(Principal principal) {
        if (principal == null) return ResponseEntity.status(403).build();
        return folioService.getMyDevBasicState(principal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build()); // 204
    }

    // ② 마이페이지 요약용: 내 최신 DRAFT/PUBLISHED 모두
    @GetMapping("/me")
    public ResponseEntity<?> getMyFoliosSummary(Principal principal) {
        if (principal == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(folioService.getMyFoliosSummary(principal));
    }
}