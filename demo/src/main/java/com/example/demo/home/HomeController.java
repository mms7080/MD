package com.example.demo.home;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.notice.Notice;
import com.example.demo.notice.NoticeRepository;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.portfolios.service.PortfolioService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {

    public final PortfoliosRepository portfoliosRepository;
    public final PortfolioService portfolioService;
    public final NoticeRepository noticeRepository;

    

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<PortfoliosEntity> portfolios = portfolioService.getPublicPortfolios();
        List<Notice> recentNotices = noticeRepository.findTop4ByOrderByCreatedAtDesc();
    
        // ✅ 통계정보 생성
        Map<String, Integer> stats = portfolioService.getPortfolioStats();
    
        System.out.println("📊 views=" + stats.get("views") + ", likes=" + stats.get("likes"));
    
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("recentNotices", recentNotices);
        model.addAttribute("stats", stats);
    
        return "home/home";
    }
    
    
    

    
    

    @GetMapping("/admin")
    public String admin() {
        return "admin/admin"; 
    }
    
}
