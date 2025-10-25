package com.example.demo.folio.controller;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FolioPublishRequest;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.service.FolioService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/folios")
@RequiredArgsConstructor
public class FolioApiController {

    private final FolioService folioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFoliosPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<FoliosSummaryDto> folioPage = folioService.getFolioSummaries(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("page", folioPage.getNumber() + 1);
        response.put("items", folioPage.getContent());
        response.put("totalPages", folioPage.getTotalPages());
        response.put("totalItems", folioPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ✅ status 있을 때
    @GetMapping(value = "/me/list", params = "status")
    public ResponseEntity<Map<String, Object>> myListByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<FoliosSummaryDto> p = folioService.getMyListByStatus(principal, status, pageable);

        Map<String, Object> body = new HashMap<>();
        body.put("page", p.getNumber());
        body.put("items", p.getContent());       // DTO 리스트만
        body.put("totalPages", p.getTotalPages());
        body.put("totalItems", p.getTotalElements());
        body.put("last", p.isLast());
        return ResponseEntity.ok(body);
    }

    // ✅ status 없을 때(전체)
    @GetMapping(value = "/me/list", params = "!status")
    public ResponseEntity<Map<String, Object>> getMyFoliosPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<FoliosSummaryDto> p = folioService.getMyListByStatus(principal, null, pageable);

        Map<String, Object> body = new HashMap<>();
        body.put("page", p.getNumber());
        body.put("items", p.getContent());
        body.put("totalPages", p.getTotalPages());
        body.put("totalItems", p.getTotalElements());
        body.put("last", p.isLast());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolioDetailDto> getFolioDetail(@PathVariable String id) {
        try {
            FolioDetailDto folioDetail = folioService.getFolioDetail(id);
            return ResponseEntity.ok(folioDetail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FolioDetailDto> createOrUpdateFolio(
            @RequestBody FolioRequestDto requestDto,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        Folio savedFolio = folioService.createOrUpdateFolio(requestDto, principal);

        // DTO 생성자가 (Folio, projects, state) 라면 아래처럼 빈값으로 채워주세요.
        FolioDetailDto responseDto = new FolioDetailDto(savedFolio, java.util.List.of(), java.util.Map.of());
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/dev-basic")
    public ResponseEntity<Map<String, Object>> saveDevBasic(
            @RequestBody FolioStateSaveRequest req,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        Folio saved = folioService.saveState(principal, req);
        return ResponseEntity.ok(java.util.Map.of("id", saved.getId()));
    }

    @PostMapping("/dev-basic/publish")
    public ResponseEntity<Map<String, Object>> publishDevBasic(
            @RequestBody FolioPublishRequest req,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(403).build();
        var saved = folioService.publishAsImages(principal, req);
        return ResponseEntity.ok(java.util.Map.of("id", saved.getId()));
    }

    @GetMapping("/me/dev-basic")
    public ResponseEntity<?> getMyDevBasic(Principal principal) {
        if (principal == null) return ResponseEntity.status(403).build();
        return folioService.getMyDevBasicState(principal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyFoliosSummary(Principal principal) {
        if (principal == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(folioService.getMyFoliosSummary(principal));
    }

    @GetMapping("/me/buckets")
    public ResponseEntity<?> getMyFoliosBuckets(Principal principal) {
        if (principal == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(folioService.getMyFoliosBuckets(principal));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable String id, Principal principal) {
        folioService.deleteIfOwner(principal, id);
        return java.util.Map.of("ok", true);
    }
}
