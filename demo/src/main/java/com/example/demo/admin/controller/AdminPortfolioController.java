package com.example.demo.admin.controller;

import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/portfolios")
@RequiredArgsConstructor
public class AdminPortfolioController {

    private final PortfoliosRepository portfoliosRepository;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    @GetMapping
    public Map<String, Object> getAllPortfolios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        List<PortfoliosEntity> all = portfoliosRepository.findAll();
        int totalItems = all.size();

        int start = Math.min(page * size, totalItems);
        int end = Math.min(start + size, totalItems);
        List<PortfoliosEntity> paginated = all.subList(start, end);

        List<Map<String, Object>> list = paginated.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("title", p.getTitle());
            map.put("creator", p.getCreator());
            map.put("desc", p.getDesc());
            map.put("cover", p.getCover());
            map.put("likeCount", p.getLikeCount());
            map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "-");

            // ✅ Lazy 컬렉션 안전 복사 (지연 초기화 방지)
            Set<String> safeTags = new LinkedHashSet<>();
            if (p.getTags() != null) safeTags.addAll(p.getTags());
            map.put("tags", safeTags);

            return map;
        }).collect(Collectors.toList());

        return Map.of(
                "portfolios", list,
                "totalItems", totalItems,
                "totalPages", (int) Math.ceil(totalItems / (double) size),
                "currentPage", page + 1
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deletePortfolio(@PathVariable Long id) {
        if (!portfoliosRepository.existsById(id)) {
            return Map.of("success", false, "message", "존재하지 않는 포트폴리오입니다.");
        }
        portfoliosRepository.deleteById(id);
        return Map.of("success", true);
    }
}
