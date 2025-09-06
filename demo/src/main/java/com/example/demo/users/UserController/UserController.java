package com.example.demo.users.UserController;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.users.UsersDTO.HeaderLogin;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final HeaderLogin keep;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @ModelAttribute
    public void addAttributes(Model model, Principal principal) {
        keep.headerlogin(model, principal); // 로그인 유지
    }

    // 로그인 페이지
    @GetMapping("/signin")
    public String signin() {
        return "users/signin"; // 로그인 HTML 경로
    }
    
}
