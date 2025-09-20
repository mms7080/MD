package com.example.demo.portfolios.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.portfolios.service.PortfolioService;

@Controller
public class PortfoliosController {

    private final PortfolioService portfolioService;
    private final PortfoliosRepository portfoliosRepository;
    
    

    PortfoliosController(PortfolioService portfolioService, PortfoliosRepository portfoliosRepository) {
        this.portfolioService = portfolioService;
        this.portfoliosRepository = portfoliosRepository;
        
    }

    @GetMapping("/portfolios")
    public String list(Model model) {
    List<PortfoliosEntity> portfolios = portfoliosRepository.findAll();
    model.addAttribute("portfolios", portfolios);
    return "portfolios/list"; 
}




@GetMapping("/portfolios/{id}")
public String getPortfolio(@PathVariable Long id, Model model) {
    // DB에서 ID로 조회
    PortfoliosEntity portfolio = portfoliosRepository.findById(id).orElse(null);

    if (portfolio == null) {
        model.addAttribute("notFound", true);
    } else {
        model.addAttribute("portfolios", portfolio);
    }

    return "portfolios/detail"; // templates/portfolios/detail.html
}



    @GetMapping("/portfolios/create")
    public String createForm(Model model){
        model.addAttribute("portfolioFormDto", new PortfolioFormDto());
        return "portfolios/create";
    }

    @PostMapping("/portfolios")
    public String create(@ModelAttribute PortfolioFormDto dto) throws IOException {
        // 1️⃣ 파일 저장 (Service 호출)
        String coverPath = portfolioService.saveFile(dto.getCover(), "image");
        String iconPath = portfolioService.saveFile(dto.getIcon(), "image");
        String downloadPath = portfolioService.saveFile(dto.getDownload(), "zip");

        // 2️⃣ DB 저장 (Service 호출)
        portfolioService.saveFromDto(dto, coverPath, iconPath, downloadPath);

        return "redirect:/portfolios";
    }

            


}
