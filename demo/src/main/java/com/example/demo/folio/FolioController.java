package com.example.demo.folio;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;



// 임시 컨트롤러
@Controller
public class FolioController {
    // 1. 목록 페이지(/folios)
    @GetMapping("folios")
    public String folioListPage(Model model){
        // Folios 목록 임시 데이터
        List<Map<String, Object>> folios = List.of(
            Map.of("id", 1L, "userName", "송준회", "mainPhthotUrl", "https://picsum.photos/seed/dev1/100","Skills", List.of("Java", "Spring Boot")),
            Map.of("id", 2L, "userName", "이홍시", "mainPhthotUrl", "https://picsum.photos/seed/dev2/100","Skills", List.of("Python", "Django"))
        );
        model.addAttribute("folios", folios); // "folios" 라는 이름으로 데이터 주머니에 담기
        return "folios/list"; // templates/folios/list.html 파일을 찾아 렌더링
    }

    // 2. 상세 페이지(/folios/detail/{id})
    @GetMapping("/folios/detail/{id}")
    public String folioDetailPage(@PathVariable Long id, Model model) {
        // 특정 id를 가진 Folio의 상세 정보 임시 데이터
        Map<String, Object> folioDetail = Map.of(
            "id", id,
            "userName", "송준회",
            "mainPhotoUrl", "https://picsum.photos/seed/dev1/400/300",
            "skills", List.of("Java, Spring Boot", "JPA", "Thymeleaf"),
            "portfolios", List.of("포트폴리오1", "포트폴리오2"),
            "introduction", "안녕하세요 신입 개발자 송준회입니다."
        );
        return "folios/detail";
    }

    // 3. 작성 페이지(/folios/write)
    @GetMapping("/folios/write")
    public String folioWritePage() {
        return "folios/form";
    }

    // 4. 관리자 페이지(/folios/append)
    @GetMapping("/folios/append")
    public String folioAdminAppendPage() {
        return "admin/folio-template-form";
    }
    
    
    
}
