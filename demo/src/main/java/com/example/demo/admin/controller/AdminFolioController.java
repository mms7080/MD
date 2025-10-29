package com.example.demo.admin.controller;

import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/folios")
@RequiredArgsConstructor
public class AdminFolioController {

    private final FolioRepository folioRepository;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    @GetMapping
    public Map<String, Object> getAllFolios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Folio> all = folioRepository.findAllWithUser();
        int totalItems = all.size();

        int start = Math.min(page * size, totalItems);
        int end = Math.min(start + size, totalItems);
        List<Folio> paginated = all.subList(start, end);

        List<Map<String, Object>> folioList = paginated.stream()
                .map(f -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("title", f.getTitle());
                    map.put("status", f.getStatus() != null ? f.getStatus().toString() : "미지정");
                    map.put("template", f.getTemplate());
                    map.put("thumbnail", f.getThumbnail());
                    map.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().format(DATE_FMT) : "-");
                    map.put("updatedAt", f.getUpdatedAt() != null ? f.getUpdatedAt().format(DATE_FMT) : "-");

                    if (f.getUser() != null) {
                        map.put("user", Map.of(
                                "name", f.getUser().getName(),
                                "username", f.getUser().getUsername()
                        ));
                    } else {
                        map.put("user", Map.of("name", "미지정"));
                    }

                    return map;
                })
                .collect(Collectors.toList());

        return Map.of(
                "folios", folioList,
                "currentPage", page + 1,
                "totalPages", (int) Math.ceil(totalItems / (double) size),
                "totalItems", totalItems
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteFolio(@PathVariable String id) {
        if (!folioRepository.existsById(id)) {
            return Map.of("success", false, "message", "존재하지 않는 Folio입니다.");
        }
        folioRepository.deleteById(id);
        return Map.of("success", true);
    }
}
