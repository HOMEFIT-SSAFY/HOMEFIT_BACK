package com.happyhome.favorite.controller;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.favorite.service.FavoriteDealService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FavoriteDealController {

    private final FavoriteDealService favoriteDealService;

    public FavoriteDealController(FavoriteDealService favoriteDealService) {
        this.favoriteDealService = favoriteDealService;
    }

    @PostMapping("/favorites/{dealNo}/toggle")
    public String toggle(
            @PathVariable int dealNo,
            @RequestParam(value = "redirect", defaultValue = "/prices") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("message", "로그인 후 관심매물을 등록할 수 있습니다.");
            return "redirect:/login";
        }

        boolean added = favoriteDealService.toggle(loginMember.getUserId(), dealNo);
        redirectAttributes.addFlashAttribute("message", added ? "관심매물에 추가했습니다." : "관심매물에서 삭제했습니다.");
        return "redirect:" + safeRedirect(redirect);
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
            return "/prices";
        }
        return redirect;
    }
}
