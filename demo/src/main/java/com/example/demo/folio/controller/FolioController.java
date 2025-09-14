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






@Controller
@RequestMapping("/folios") // 이 컨트롤러의 모든 주소는 /folios로 시작
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
    public String folioEditPage(Model model, Principal principal) {
        // 현재 로그인한 사용자 정보 조회
        Users currentUser = usersService.getUserByUsername(principal.getName());
        // 모델에 사용자 정보 담아서 전달
        model.addAttribute("currentUser", currentUser);
        return "folios/edit";
    }
    
}
    
    
    
    
    

