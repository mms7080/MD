package com.example.demo.home;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.portfolios.service.PortfolioService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {

    public final PortfoliosRepository portfoliosRepository;
    public final PortfolioService portfolioService;
    
    @GetMapping({"/", "/home"})
public String home(Model model) {
    List<PortfoliosEntity> portfolios = portfolioService.getPublicPortfolios();
    model.addAttribute("portfolios", portfolios);
    return "home/home";
}

    
    

    @GetMapping("/admin")
    public String admin() {
        return "admin/admin"; 
    }
    
}
