package com.example.demo.users.UserController;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersService.UsersService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final UsersService usersService;

    /** 회원가입 폼 */
    @GetMapping("/signup")
    public String signupForm(@ModelAttribute("usersDTO") UsersDTO usersDTO) {
        return "users/signup";
    }

    /** 회원가입 처리 */
    @PostMapping("/signup")
    public String signupProcess(
            @Validated(UsersDTO.Create.class)
            @ModelAttribute("usersDTO") UsersDTO usersDTO,
            BindingResult bindingResult,
            @RequestParam("emailCode") String emailCode,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        // 비밀번호 확인 검증
        if (usersDTO.getConfirmPassword() != null &&
            !usersDTO.getConfirmPassword().equals(usersDTO.getPassword())) {
            bindingResult.rejectValue("confirmPassword", "Mismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usersDTO", bindingResult);
            redirectAttributes.addFlashAttribute("usersDTO", usersDTO);
            return "redirect:/signup";
        }

        try {
            usersService.registerUser(usersDTO, emailCode, httpSession); // ⬅️ 세션 전달
            redirectAttributes.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/signin";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("usersDTO", usersDTO);
            return "redirect:/signup";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알 수 없는 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("usersDTO", usersDTO);
            return "redirect:/signup";
        }
    }

    @GetMapping("/api/check-username")
    @ResponseBody
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = usersService.isUsernameTaken(username); 
        return ResponseEntity.ok(exists);
    }
}
