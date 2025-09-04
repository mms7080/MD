package com.example.demo.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/","/home"})
    public String home() {
        return "home/home"; 
    }

    @GetMapping("/detail")
    public String detail() {
        return "detail/detail"; 
    }
}
