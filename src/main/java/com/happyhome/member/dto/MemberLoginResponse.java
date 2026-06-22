package com.happyhome.member.dto;

public record MemberLoginResponse(
        String grantType,
        String accessToken,
        String refreshToken,
        MemberResponse member
) {
}
