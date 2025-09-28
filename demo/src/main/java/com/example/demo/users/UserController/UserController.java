package com.example.demo.users.UserController;

import java.security.Principal;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.users.UsersDTO.HeaderLogin;
import com.example.demo.users.UsersDTO.ProfileDTO;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class UserController {

    private final HeaderLogin keep;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @ModelAttribute
    public void addAttributes(Model model, Principal principal) {
        keep.headerlogin(model, principal); // 로그인 유지
    }

    @GetMapping("/home")
    public String mypage(Model model, Principal principal) {
        // 1) 비로그인 접근 가드
        if (principal == null) {
            return "redirect:/home?modal=signin";
        }
        Users users = usersService.getUserByUsername(principal.getName());
        ProfileDTO profile = usersService.loadProfileForm(users.getId());
        model.addAttribute("form", profile);
        return "mypage/mypage";
    }

    @GetMapping("/team")
    public String mypageTeam() {
        return "mypage/team";
    }

    @GetMapping("/withdraw")
    public String withdrawForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/home?modal=signin";
        }
        Users users = usersService.getUserByUsername(principal.getName());
        model.addAttribute("users", users);
        return "mypage/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("password") String password,
            Principal principal,
            HttpServletRequest req,
            HttpServletResponse res) {
        usersService.withdrawSelf(principal.getName(), password);
        new SecurityContextLogoutHandler().logout(req, res, null);
        return "redirect:/home";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam("name") String name,
            @RequestParam(value = "githubUrl", required = false) String githubUrl,
            @RequestParam(value = "positions", required = false) java.util.Set<String> positions,
            @RequestParam(value = "profileImgUrl", required = false) String profileImgUrlHidden,
            Principal principal) {
        if (principal == null)
            return "redirect:/home?modal=signin";

        Users me = usersService.getUserByUsername(principal.getName());
        usersService.updateProfileBasic(
                me.getId(),
                name,
                githubUrl,
                positions,
                profileImgUrlHidden // 그대로 유지
        );
        return "redirect:/mypage/home";
    }
}
