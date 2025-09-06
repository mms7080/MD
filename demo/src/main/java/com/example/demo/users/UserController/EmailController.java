package com.example.demo.users.UserController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersService.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    /** 이메일 인증 코드 발송 */
    @PostMapping("/send-code")
    public String sendCode(@RequestParam String email, Model model) {
        try {
            emailVerificationService.sendCode(email);
            model.addAttribute("msg", "인증 코드가 발송되었습니다. 메일함을 확인하세요.");
        } catch (Exception e) {
            model.addAttribute("error", "메일 발송 실패: " + e.getMessage());
        }
        // 다시 회원가입 폼으로 이동
        model.addAttribute("usersDTO", new UsersDTO());
        return "users/signup"; // signup.html
    }
}
