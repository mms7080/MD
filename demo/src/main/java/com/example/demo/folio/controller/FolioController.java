package com.example.demo.folio.controller;

import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersService.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/folios") // 뷰 페이지
@RequiredArgsConstructor
public class FolioController {

    private final UsersService usersService;

    // 1. 목록 페이지 뷰
    @GetMapping
    public String folioListPage(Model model){
        return "folios/list";
    }

    // 2. 상세 페이지 뷰
    @GetMapping("/detail/{id}")
    public String folioDetailPage(@PathVariable String id, Model model) {
        model.addAttribute("folioId", id);
        return "folios/detail";
    }

    // 3. 작성 페이지 뷰 (로그인 필요: SecurityConfig에서 보호)
    @GetMapping("/write")
    public String folioWritePage() {
        return "folios/write";
    }

    // 4. 관리자용 Append 페이지 뷰(선택)
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
        model.addAttribute("template", template);
        return "folios/edit";
    }
}
