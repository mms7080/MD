package com.example.demo.users.UserController;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.users.UsersService.UsersService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FindSignController {

    private final UsersService usersService;

    @GetMapping("/forgot")
    public String searchSignInfo() {
        return "users/find";
    }

    // 아이디 찾기: 마스킹된 아이디 안내
    @PostMapping("/forgot/id")
    public String findUsername(@RequestParam("name") String name,
            @RequestParam("email") String email,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<String> masked = usersService.findUsernameBy(name, email); // 서비스에서 마스킹까지 처리했다고 가정
            if (masked.isPresent()) {
                model.addAttribute("maskedId", masked.get());
                model.addAttribute("ok", true);
                return "users/find-id";
            } else {
                redirectAttributes.addFlashAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
                return "redirect:/forgot";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot";
        }
    }

    @PostMapping("/forgot/password")
    public String changeForgotPassword(@RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("newpassword") String newpassword,
            @RequestParam("confirmpassword") String confirmpassword,
            RedirectAttributes redirectAttributes) {
        try {
            usersService.changeForgotPassword(username, name, email, newpassword, confirmpassword);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인하세요.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot?tab=pw";
        }
        return "redirect:/home?modal=signin";
    }

}
