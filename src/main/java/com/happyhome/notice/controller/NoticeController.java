package com.happyhome.notice.controller;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.notice.dto.NoticeDto;
import com.happyhome.notice.dto.NoticeRequest;
import com.happyhome.notice.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/notices")
    public String notices(Model model) {
        model.addAttribute("notices", noticeService.findVisible(50));
        return "notices";
    }

    @GetMapping("/notices/new")
    public String newNotice(Model model, HttpSession session) {
        if (!model.containsAttribute("notice")) {
            NoticeDto notice = new NoticeDto();
            notice.setVisible(true);
            notice.setWriter(currentWriter(session));
            model.addAttribute("notice", notice);
        }
        model.addAttribute("mode", "create");
        return "notice-form";
    }

    @PostMapping("/notices")
    public String create(NoticeDto form, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            NoticeDto created = noticeService.create(toRequest(form), currentWriter(session));
            redirectAttributes.addFlashAttribute("message", "공지사항이 등록되었습니다.");
            return "redirect:/notices/" + created.getNoticeId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("notice", form);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/notices/new";
        }
    }

    @GetMapping("/notices/{noticeId}")
    public String detail(@PathVariable int noticeId, Model model) {
        NoticeDto notice = noticeService.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        model.addAttribute("notice", notice);
        return "notice-detail";
    }

    @GetMapping("/notices/{noticeId}/edit")
    public String editForm(@PathVariable int noticeId, Model model) {
        NoticeDto notice = noticeService.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        model.addAttribute("notice", notice);
        model.addAttribute("mode", "edit");
        return "notice-form";
    }

    @PostMapping("/notices/{noticeId}")
    public String update(
            @PathVariable int noticeId,
            NoticeDto form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noticeService.update(noticeId, toRequest(form), currentWriter(session))
                    .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
            redirectAttributes.addFlashAttribute("message", "공지사항이 수정되었습니다.");
            return "redirect:/notices/" + noticeId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("notice", form);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/notices/" + noticeId + "/edit";
        }
    }

    @PostMapping("/notices/{noticeId}/delete")
    public String delete(@PathVariable int noticeId, RedirectAttributes redirectAttributes) {
        noticeService.delete(noticeId);
        redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        return "redirect:/notices";
    }

    private NoticeRequest toRequest(NoticeDto form) {
        return new NoticeRequest(
                form.getTitle(),
                form.getContent(),
                form.getWriter(),
                form.isPopupEnabled(),
                form.isPinned(),
                form.isVisible()
        );
    }

    private String currentWriter(HttpSession session) {
        MemberDto member = (MemberDto) session.getAttribute("loginMember");
        return member == null ? "admin" : member.getUserId();
    }
}
