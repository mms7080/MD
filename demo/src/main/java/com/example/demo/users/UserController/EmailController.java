package com.example.demo.users.UserController;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.users.UsersService.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    /** 이메일 인증 코드 발송 */
     @PostMapping("/api/send-code")
    public ResponseEntity<Boolean> sendCode(@RequestParam String email, HttpSession session) {
        try {
            emailVerificationService.sendCode(email, session);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
