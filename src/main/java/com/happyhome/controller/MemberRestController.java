package com.happyhome.controller;

import com.happyhome.dto.MemberAuthRequest;
import com.happyhome.dto.MemberDto;
import com.happyhome.dto.MemberResponse;
import com.happyhome.dto.MemberUpdateRequest;
import com.happyhome.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "회원가입, 로그인, 내 정보 API")
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원가입", description = "회원 정보를 등록하고 비밀번호는 BCrypt로 저장합니다.")
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@RequestBody MemberDto member) {
        if (!memberService.register(member)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 검증하고 세션에 로그인 정보를 저장합니다.")
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody MemberAuthRequest request, HttpSession session) {
        return memberService.authenticate(request.getUserId(), request.getPassword())
                .map(member -> {
                    memberService.upgradePasswordIfLegacy(request.getUserId(), request.getPassword());
                    MemberDto refreshed = memberService.findByUserId(request.getUserId()).orElse(member);
                    session.setAttribute("loginMember", refreshed);
                    saveSecurityContext(session, refreshed);
                    return ResponseEntity.ok(MemberResponse.from(refreshed));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "로그아웃", description = "현재 세션과 보안 컨텍스트를 제거합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        ResponseCookie expiredSessionCookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredSessionCookie.toString())
                .build();
    }

    private void saveSecurityContext(HttpSession session, MemberDto member) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                member.getUserId(),
                null,
                List.of(new SimpleGrantedAuthority(resolveAuthority(member)))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private String resolveAuthority(MemberDto member) {
        if ("admin".equals(member.getUserId())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }

    @Operation(summary = "내 정보 조회", description = "현재 세션에 로그인된 회원 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(MemberResponse.from(loginMember));
    }

    @Operation(summary = "내 정보 수정", description = "현재 세션에 로그인된 회원의 이름, 이메일, 전화번호, 비밀번호를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(@RequestBody MemberUpdateRequest request, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return memberService.update(loginMember.getUserId(), request.toMemberDto())
                .map(member -> {
                    session.setAttribute("loginMember", member);
                    return ResponseEntity.ok(MemberResponse.from(member));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
