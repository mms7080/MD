package com.example.demo.Config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.users.UsersDTO.HeaderLogin;



@Controller
public class CustomErrorController implements ErrorController{
	
	@Autowired
	HeaderLogin keep;
	
	
	@ModelAttribute //모든 매핑에 추가할 코드
    public void addAttributes(Model model, Principal principal) {
        keep.headerlogin(model, principal); //로그인 유지 
    }
	
	@GetMapping("/error")
	public String handleError() {
		return "error/error";
	}
}
