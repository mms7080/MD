package com.example.demo.folio.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersService.UsersService;
import java.security.Principal;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;
import com.example.demo.folio.dto.PortfolioInFolioDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.service.FolioService; // FolioService 주입
import java.util.List;






@Controller
@RequestMapping("/folios") // 이 컨트롤러의 모든 주소는 /folios로 시작
@RequiredArgsConstructor
public class FolioController {

    private final UsersService usersService;
    private final FolioService folioService; 

    // 1. 목록 페이지 뷰
    @GetMapping
    public String folioListPage(Model model){
        return "folios/list";
    }

    // 2. 상세 페이지 뷰
    @GetMapping("/detail/{id}")
    public String folioDetailPage(@PathVariable String id, Model model) {
        // 상세 페이지 로직은 나중에 API와 함께 구현합니다.
        model.addAttribute("folioId", id);
        return "folios/detail";
    }

    // 3. 작성 페이지 뷰
    @GetMapping("/write")
    public String folioWritePage() {
        return "folios/write";
    }

    // 4. 관리자용 Append 페이지 뷰
    @GetMapping("/append")
    public String folioAdminAppendPage() {
        return "folios/append";
    }

    // 5. 편집 페이지 뷰
    @GetMapping("/edit")
    public String folioEditPage(
            @RequestParam(name = "template", required = false, defaultValue = "dev-basic") String template,
            Model model,
            Principal principal
    ) {
        if (principal != null) {
            Users currentUser = usersService.getUserByUsername(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("template", template); // edit.html에서 ${template} 사용
        return "folios/edit";
    }

    // 혹시 기존 북마크/링크 대비: /ppt → /edit로 리다이렉트
    @GetMapping("/ppt")
    public String pptRedirect(
            @RequestParam(name = "template", required = false) String template
    ) {
        String q = (template != null && !template.isBlank())
                ? "?template=" + java.net.URLEncoder.encode(template, java.nio.charset.StandardCharsets.UTF_8)
                : "";
        return "redirect:/folios/edit" + q;
    }

}
    
    
    
    
    

