package com.example.demo.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;

// 공지목록
@GetMapping
public String list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String keyword,
        Model model) {

    int pageSize = 5;

    // 🔹 id 기준 내림차순 정렬 (최신글 위로)
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

    Page<Notice> noticePage;

    if (keyword != null && !keyword.isBlank()) {
        if ("writer".equals(type)) {
            noticePage = noticeRepository.findByWriterContaining(keyword, pageable);
        } else { // 기본은 제목 검색
            noticePage = noticeRepository.findByTitleContaining(keyword, pageable);
        }
    } else {
        noticePage = noticeRepository.findAll(pageable);
    }

    int totalPages = noticePage.getTotalPages();
    int currentPage = page;
    int blockSize = 5;
    int startPage = (currentPage / blockSize) * blockSize;
    int endPage = Math.min(startPage + blockSize - 1, totalPages - 1);

    model.addAttribute("notices", noticePage.getContent());
    model.addAttribute("currentPage", currentPage);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("startPage", startPage);
    model.addAttribute("endPage", endPage);
    model.addAttribute("type", type);
    model.addAttribute("keyword", keyword);
    model.addAttribute("isEmpty", noticePage.isEmpty());

    return "notice/list";
}

    // 공지사항 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id).orElseThrow();

        // 이전글
        Notice prevNotice = noticeRepository.findFirstByIdLessThanOrderByIdDesc(id).orElse(null);
        // 다음글
        Notice nextNotice = noticeRepository.findFirstByIdGreaterThanOrderByIdAsc(id).orElse(null);

        model.addAttribute("notice", notice);
        model.addAttribute("prevNotice", prevNotice);
        model.addAttribute("nextNotice", nextNotice);

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
