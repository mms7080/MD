package com.example.demo.users.UserController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import com.example.demo.users.UsersDTO.UsersDTO;
import com.example.demo.users.UsersService.UsersService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final UsersService usersService;

    /** 회원가입 폼 */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("usersDTO", new UsersDTO());
        return "users/signup";
    }

    /** 회원가입 처리 */
    @PostMapping("/signup")
    public String signupProcess(
            @Validated(UsersDTO.Create.class) 
            @ModelAttribute("usersDTO") UsersDTO usersDTO,
            @RequestParam("emailCode") String emailCode,
            BindingResult bindingResult,
            Model model
    ) {
        if (usersDTO.getConfirmPassword() != null &&
            !usersDTO.getConfirmPassword().equals(usersDTO.getPassword())) {
            bindingResult.rejectValue("confirmPassword", "Mismatch", "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "users/signup";
        }

        try {
            usersService.registerUser(usersDTO, emailCode);
            model.addAttribute("msg", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "users/login"; // 또는 redirect:/login
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "users/signup";
        } catch (Exception e) {
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
            return "users/signup";
        }
    }
}