package com.example.demo.folio.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersService.UsersService;

import java.security.Principal;

import com.example.demo.folio.service.FolioService; // FolioService 주입



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
        var detail = folioService.getFolioDetail(id);

        model.addAttribute("folio", detail);
        model.addAttribute("state", detail.getState() != null ? detail.getState() : java.util.Map.of());

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


}