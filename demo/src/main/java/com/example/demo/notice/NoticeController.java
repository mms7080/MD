package com.example.demo.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    // 공지사항 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("notices", noticeRepository.findAll());
        return "notice/list";
    }

    // 공지사항 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        model.addAttribute("notice", notice);
        return "notice/detail";
    }

    // 공지 작성 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("notice", new Notice()); 
        return "notice/noticeWrite";
    }

    // 공지 저장 처리
    @PostMapping
    public String create(@ModelAttribute Notice notice) {
        noticeRepository.save(notice);
        return "redirect:/notice";
    }
}
