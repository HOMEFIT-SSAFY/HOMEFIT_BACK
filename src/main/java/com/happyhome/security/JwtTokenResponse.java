package com.happyhome.security;

public record JwtTokenResponse(
        String grantType,
        String accessToken,
        String refreshToken
) {
    public static JwtTokenResponse bearer(String accessToken, String refreshToken) {
        return new JwtTokenResponse("Bearer", accessToken, refreshToken);
    }
}
