package com.example.demo.folio.controller;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.service.FolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.entity.Folio;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/folios") // API 경로를 명세서에 맞게 /api/folios로 변경
@RequiredArgsConstructor
public class FolioApiController {

    private final FolioService folioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFoliosPage() {
        List<FoliosSummaryDto> summaries = folioService.getFolioSummaries();
        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);
        response.put("items", summaries);
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
            // 로그인하지 않은 사용자는 권한 없음(401) 또는 접근 거부(403) 응답
            return ResponseEntity.status(403).build();
        }
        
        Folio savedFolio = folioService.createOrUpdateFolio(requestDto, principal);
        
        // 저장 후 상세 DTO로 변환하여 반환
        FolioDetailDto responseDto = new FolioDetailDto(savedFolio);
        return ResponseEntity.ok(responseDto);
    }
    
    

}