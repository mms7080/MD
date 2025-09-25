package com.example.demo.portfolios.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;
// import com.example.demo.portfolios.service.PortfolioLikeService;
import com.example.demo.portfolios.service.PortfolioService;

@Controller
@RequestMapping("portfolios")
public class PortfoliosController {

    private final PortfolioService portfolioService;
    private final PortfoliosRepository portfoliosRepository;
    
    
    

    PortfoliosController(PortfolioService portfolioService, PortfoliosRepository portfoliosRepository) {
        this.portfolioService = portfolioService;
        this.portfoliosRepository = portfoliosRepository;
        
    }

    @GetMapping
public String list(Model model,
   @RequestParam(defaultValue = "0") int page,
   @RequestParam(defaultValue = "12") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<PortfoliosEntity> portfoliosPage = portfoliosRepository.findAllWithTags(pageable);

    // ✅ 디버그용 로그
    System.out.println("포트폴리오 개수: " + portfoliosPage.getContent().size());
    portfoliosPage.getContent().forEach(p -> 
        System.out.println("제목: " + p.getTitle())
    );

    model.addAttribute("portfoliosPage", portfoliosPage);
    model.addAttribute("portfolios", portfoliosPage.getContent());
    return "portfolios/list";
}




@GetMapping("/{id}")
public String getPortfolio(@PathVariable Long id, Model model) {
    PortfoliosEntity portfolio = portfoliosRepository.findDetailById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

    // ✅ 중복 제거
    portfolio.setScreenshots(new ArrayList<>(new LinkedHashSet<>(portfolio.getScreenshots())));
    portfolio.setTeam(new ArrayList<>(new LinkedHashSet<>(portfolio.getTeam())));

    model.addAttribute("portfolio", portfolio);
    return "portfolios/detail";
}




    @GetMapping("/create")
    public String createForm(Model model){
        model.addAttribute("portfolioFormDto", new PortfolioFormDto());
        return "portfolios/create";
    }

    @PostMapping
    public String create(@ModelAttribute PortfolioFormDto dto) throws IOException {
        // 1️⃣ 파일 저장 (Service 호출)
        String coverPath = portfolioService.saveFile(dto.getCover(), "image");
        String iconPath = portfolioService.saveFile(dto.getIcon(), "image");
        String downloadPath = portfolioService.saveFile(dto.getDownload(), "zip");

        // 2️⃣ DB 저장 (Service 호출)
        portfolioService.saveFromDto(dto, coverPath, iconPath, downloadPath);

        return "redirect:/portfolios";
    }

    // delete
    @PostMapping("/delete/{id}")
    public String deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return "redirect:/portfolios"; // 삭제 후 포트폴리오 리스트 페이지로 이동
    }

//     @PostMapping("/{id}/like")
// public String likePortfolio(@PathVariable Long id, Principal principal) {
//     portfolioService.likePortfolio(id, principal.getName());
//     return "redirect:/portfolios/" + id;  // 다시 상세 페이지로 이동
// }

// @PostMapping("/{id}/unlike")
// public String unlikePortfolio(@PathVariable Long id, Principal principal) {
//     portfolioService.unlikePortfolio(id, principal.getName());
//     return "redirect:/portfolios/" + id;
// }

            


}
