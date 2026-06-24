package com.happyhome.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class OAuth2LoginSuccessHandlerTest {

    @Test
    void redirectsWithJwtTokensAfterOauthLogin() throws Exception {
        MemberService memberService = mock(MemberService.class);
        JwtProvider jwtProvider = mock(JwtProvider.class);
        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(memberService, jwtProvider);
        MemberDto member = member("oauth_google_123");
        when(memberService.findOrCreateOAuthMember(eq("google"), eq("123"), eq("user@example.com"), eq("사용자")))
                .thenReturn(member);
        when(jwtProvider.createToken(any(MemberDto.class)))
                .thenReturn(new JwtTokenResponse("Bearer", "access-token", "refresh-token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(OAuthRedirectController.REDIRECT_SESSION_KEY, "http://localhost:5173/prices?mode=search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, googleAuthentication());

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith("http://localhost:5173/prices?mode=search");
        assertThat(redirect).contains("oauth=success");
        assertThat(redirect).contains("grantType=Bearer");
        assertThat(redirect).contains("accessToken=access-token");
        assertThat(redirect).contains("refreshToken=refresh-token");
        verify(jwtProvider).createToken(member);
    }

    private OAuth2AuthenticationToken googleAuthentication() {
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", "123",
                        "email", "user@example.com",
                        "name", "사용자"
                ),
                "sub"
        );
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
    }

    private MemberDto member(String userId) {
        MemberDto member = new MemberDto();
        member.setUserId(userId);
        member.setEmail("user@example.com");
        member.setName("사용자");
        return member;
    }
}
