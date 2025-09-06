package com.example.demo.users.UserController;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersService.EmailVerificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    /** 이메일 인증 코드 발송 */
    @PostMapping("/send-code")
    public String sendCode(@RequestParam String email,
                           HttpSession session,
                           RedirectAttributes ra) {
        try {
            emailVerificationService.sendCode(email, session); // ⬅️ 세션 전달
            ra.addFlashAttribute("msg", "인증 코드가 발송되었습니다. 메일함을 확인하세요.");
            // 사용자가 입력했던 이메일을 폼에 유지하고 싶다면:
            UsersDTO dto = new UsersDTO();
            dto.setEmail(email);
            ra.addFlashAttribute("usersDTO", dto);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "메일 발송 실패: " + e.getMessage());
        }
        // PRG: 새로고침 중복 방지
        return "redirect:/signup";
    }
}
