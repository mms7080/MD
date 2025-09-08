package com.example.demo.users.UsersDTO;

import java.security.Principal;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class HeaderLogin {
 public void headerlogin(Model model, Principal principal) {
        if (principal != null) {
            // 사용자가 로그인한 경우
            model.addAttribute("loggedIn", true);
            model.addAttribute("username", principal.getName());
        } else {
            // 사용자가 로그인하지 않은 경우
            model.addAttribute("loggedIn", false);
        }
    }
}
