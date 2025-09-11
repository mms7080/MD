package com.example.demo.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    //공지 목록
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Notice> noticePage;

        if (keyword != null && !keyword.isBlank()) {
            if ("writer".equals(type)) {
                noticePage = noticeRepository.findByWriterContaining(keyword, pageable);
            } else {
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

    //공지 상세 
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지를 찾을 수 없습니다. id=" + id));

        // 조회수 증가 
        notice.setViews(notice.getViews() + 1);
        noticeRepository.saveAndFlush(notice);

        Notice prevNotice = noticeRepository.findFirstByIdLessThanOrderByIdDesc(id).orElse(null);
        Notice nextNotice = noticeRepository.findFirstByIdGreaterThanOrderByIdAsc(id).orElse(null);

        model.addAttribute("notice", notice);
        model.addAttribute("prevNotice", prevNotice);
        model.addAttribute("nextNotice", nextNotice);

        return "notice/detail";
    }

    //공지 작성 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("notice", new Notice());
        return "notice/noticeWrite";
    }

    //공지 저장 처리
    @PostMapping
    public String create(@ModelAttribute Notice notice) {
        noticeRepository.save(notice);
        return "redirect:/notice";
    }

    //공지 수정 폼
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지를 찾을 수 없습니다. id=" + id));
        model.addAttribute("notice", notice);
        return "notice/noticeEdit";
    }

    //공지 수정 처리
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Notice formNotice) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지를 찾을 수 없습니다. id=" + id));

        notice.setTitle(formNotice.getTitle());
        notice.setWriter(formNotice.getWriter());
        notice.setContent(formNotice.getContent());

        noticeRepository.save(notice);
        return "redirect:/notice/" + id;
    }

    //공지 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        noticeRepository.deleteById(id);
        return "redirect:/notice";
    }

    @PostMapping("/{id}/increase-views")
    @ResponseBody
    public void increaseViews(@PathVariable Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지 없음 id=" + id));
        notice.setViews(notice.getViews() + 1);
        noticeRepository.save(notice);
    }

    
}
