package com.happyhome.security;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    public OAuth2LoginSuccessHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect("/home");
            return;
        }

        OAuth2Profile profile = OAuth2Profile.from(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getPrincipal()
        );
        MemberDto member = memberService.findOrCreateOAuthMember(
                profile.provider(),
                profile.providerUserId(),
                profile.email(),
                profile.name()
        );

        HttpSession session = request.getSession();
        session.setAttribute("loginMember", member);
        saveSecurityContext(session, member);

        String redirect = (String) session.getAttribute(OAuthRedirectController.REDIRECT_SESSION_KEY);
        session.removeAttribute(OAuthRedirectController.REDIRECT_SESSION_KEY);
        response.sendRedirect(redirect == null || redirect.isBlank() ? "/home" : redirect);
    }

    private void saveSecurityContext(HttpSession session, MemberDto member) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                member.getUserId(),
                null,
                List.of(new SimpleGrantedAuthority(resolveAuthority(member)))
        ));
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private String resolveAuthority(MemberDto member) {
        if ("admin".equals(member.getUserId())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }

    private record OAuth2Profile(String provider, String providerUserId, String email, String name) {

        static OAuth2Profile from(String provider, OAuth2User user) {
            Map<String, Object> attributes = user.getAttributes();
            return switch (provider) {
                case "kakao" -> kakao(provider, attributes);
                case "naver" -> naver(provider, attributes);
                default -> googleLike(provider, attributes);
            };
        }

        @SuppressWarnings("unchecked")
        private static OAuth2Profile kakao(String provider, Map<String, Object> attributes) {
            Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
            Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
            return new OAuth2Profile(
                    provider,
                    stringValue(attributes.get("id")),
                    stringValue(account.get("email")),
                    stringValue(profile.get("nickname"))
            );
        }

        @SuppressWarnings("unchecked")
        private static OAuth2Profile naver(String provider, Map<String, Object> attributes) {
            Map<String, Object> response = (Map<String, Object>) attributes.getOrDefault("response", Map.of());
            return new OAuth2Profile(
                    provider,
                    stringValue(response.get("id")),
                    stringValue(response.get("email")),
                    stringValue(response.get("name"))
            );
        }

        private static OAuth2Profile googleLike(String provider, Map<String, Object> attributes) {
            return new OAuth2Profile(
                    provider,
                    stringValue(attributes.getOrDefault("sub", attributes.get("id"))),
                    stringValue(attributes.get("email")),
                    stringValue(attributes.get("name"))
            );
        }

        private static String stringValue(Object value) {
            return value == null ? "" : String.valueOf(value);
        }
    }
}
