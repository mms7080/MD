    package com.example.demo.users.UserController;

    import java.security.Principal;

    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;

    import com.example.demo.users.UsersDTO.HeaderLogin;
    import com.example.demo.users.UsersEntity.Users;
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

        
    @GetMapping("/mypage")
    public String mypage(Model model, Principal principal) {
        // 1) 비로그인 접근 가드
        if (principal == null) {
            return "redirect:/home?modal=signin";
        }
        Users users = usersService.getUserByUsername(principal.getName());
        model.addAttribute("users", users);
        return "mypage/mypage";
    }

    @GetMapping("/mypage/team")
    public String mypageTeam() {
        return "mypage/team";
    }

    }
