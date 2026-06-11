package com.happyhome.security;

import com.happyhome.member.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    public LoginSuccessHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        String rawPassword = request.getParameter("password");
        if (StringUtils.hasText(rawPassword)) {
            memberService.upgradePasswordIfLegacy(authentication.getName(), rawPassword);
        }
        memberService.findByUserId(authentication.getName())
                .ifPresent(member -> request.getSession().setAttribute("loginMember", member));
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
