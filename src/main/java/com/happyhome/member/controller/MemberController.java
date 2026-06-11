package com.happyhome.member.controller;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.favorite.service.FavoriteDealService;
import com.happyhome.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final FavoriteDealService favoriteDealService;

    public MemberController(MemberService memberService, FavoriteDealService favoriteDealService) {
        this.memberService = memberService;
        this.favoriteDealService = favoriteDealService;
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        if (request.getParameter("error") != null) {
            model.addAttribute("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("member")) {
            model.addAttribute("member", new MemberDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(MemberDto member, RedirectAttributes redirectAttributes) {
        if (!memberService.register(member)) {
            redirectAttributes.addFlashAttribute("member", member);
            redirectAttributes.addFlashAttribute("message", "이미 사용 중인 아이디이거나 입력값이 부족합니다.");
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
        return "redirect:/login";
    }

    @GetMapping("/member")
    public String member(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember != null) {
            model.addAttribute("favoriteDeals", favoriteDealService.findFavoriteDeals(loginMember.getUserId(), 50));
        }
        return "member";
    }

    @GetMapping("/member/edit")
    public String editForm() {
        return "member-edit";
    }

    @PostMapping("/member/edit")
    public String edit(MemberDto form, HttpSession session, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        MemberDto member = memberService.update(loginMember.getUserId(), form)
                .orElseThrow(() -> new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다."));
        session.setAttribute("loginMember", member);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
        return "redirect:/member";
    }

    @GetMapping("/member/delete")
    public String delete(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember != null) {
            memberService.delete(loginMember.getUserId());
        }
        session.invalidate();
        return "redirect:/home";
    }

    @GetMapping("/password-find")
    public String passwordFindForm() {
        return "password-find";
    }

    @PostMapping("/password-find")
    public String passwordFind(MemberDto form, Model model) {
        if (!StringUtils.hasText(form.getUserId()) || !StringUtils.hasText(form.getEmail())) {
            model.addAttribute("error", "아이디와 이메일을 입력해 주세요.");
            return "password-find";
        }
        memberService.findByUserIdAndEmail(form.getUserId(), form.getEmail())
                .ifPresentOrElse(
                        member -> model.addAttribute("message", "가입 정보가 확인되었습니다. 비밀번호 재설정은 관리자에게 문의해 주세요."),
                        () -> model.addAttribute("error", "일치하는 회원 정보가 없습니다.")
                );
        return "password-find";
    }
}
