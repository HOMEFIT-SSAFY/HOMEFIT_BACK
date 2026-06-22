package com.happyhome.member.controller;

import com.happyhome.member.dto.FinancialProfile;
import com.happyhome.member.dto.MemberAuthRequest;
import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.dto.MemberLoginResponse;
import com.happyhome.member.dto.MemberResponse;
import com.happyhome.member.dto.MemberUpdateRequest;
import com.happyhome.member.service.FinancialProfileService;
import com.happyhome.member.service.MemberService;
import com.happyhome.security.JwtProvider;
import com.happyhome.security.JwtTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Member registration and JWT authentication API")
public class MemberRestController {

    private final MemberService memberService;
    private final FinancialProfileService financialProfileService;
    private final JwtProvider jwtProvider;

    public MemberRestController(
            MemberService memberService,
            FinancialProfileService financialProfileService,
            JwtProvider jwtProvider
    ) {
        this.memberService = memberService;
        this.financialProfileService = financialProfileService;
        this.jwtProvider = jwtProvider;
    }

    @Operation(summary = "Register member")
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@RequestBody MemberDto member) {
        if (!memberService.register(member)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @Operation(summary = "Login and issue JWT")
    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> login(@RequestBody MemberAuthRequest request) {
        return memberService.authenticate(request.getUserId(), request.getPassword())
                .map(member -> {
                    memberService.upgradePasswordIfLegacy(request.getUserId(), request.getPassword());
                    MemberDto refreshed = memberService.findByUserId(request.getUserId()).orElse(member);
                    JwtTokenResponse token = jwtProvider.createToken(refreshed);
                    return ResponseEntity.ok(new MemberLoginResponse(
                            token.grantType(),
                            token.accessToken(),
                            token.refreshToken(),
                            MemberResponse.from(refreshed)
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
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

    @Operation(summary = "Get current member")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(Authentication authentication) {
        return currentMember(authentication)
                .map(MemberResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Update current member")
    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @RequestBody MemberUpdateRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return memberService.update(authentication.getName(), request.toMemberDto())
                .map(MemberResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me/financial-profile")
    public ResponseEntity<FinancialProfile> financialProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return financialProfileService.findByUserId(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/me/financial-profile")
    public ResponseEntity<FinancialProfile> updateFinancialProfile(
            @RequestBody FinancialProfile request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(financialProfileService.save(authentication.getName(), request));
    }

    private Optional<MemberDto> currentMember(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        return memberService.findByUserId(authentication.getName());
    }
}
