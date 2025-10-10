package com.example.demo.users.UserController;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.users.UsersService.UsersService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class FinfSignController {
        
    private final UsersService usersService;

    @GetMapping("/forgot")
    public String searchSignInfo() {
        return "users/find";
    }

    /** 아이디 찾기: 이름+이메일 → 마스킹된 아이디 안내 */
    @PostMapping("/forgot/id")
    public String findUsername(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               RedirectAttributes ra) {

        try {
            Optional<String> masked = usersService.findUsernameBy(name, email); // 서비스에서 마스킹까지 처리했다고 가정
            if (masked.isPresent()) {
                ra.addFlashAttribute("message", "회원님의 아이디는 '" + masked.get() + "' 입니다.");
            } else {
                ra.addFlashAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot";
    }
    
}
