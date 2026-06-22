package com.happyhome.security;

import com.happyhome.member.dto.MemberDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtProvider {

    private static final String AUTH_CLAIM = "auth";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TYPE = "ACCESS";
    private static final String REFRESH_TYPE = "REFRESH";

    private final SecretKey key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMillis,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMillis
    ) {
        this.key = Keys.hmacShaKeyFor(decodeSecretKey(secret));
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    public JwtTokenResponse createToken(MemberDto member) {
        String authority = resolveAuthority(member.getUserId());
        return JwtTokenResponse.bearer(
                createAccessToken(member.getUserId(), authority),
                createRefreshToken(member.getUserId())
        );
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TYPE.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public String getRefreshSubject(String token) {
        Claims claims = parseClaims(token);
        if (!REFRESH_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Token is not a refresh token.");
        }
        return claims.getSubject();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (!ACCESS_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Token is not an access token.");
        }
        String authorities = claims.get(AUTH_CLAIM, String.class);
        List<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(authorities.split(","))
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, grantedAuthorities);
    }

    private String createAccessToken(String userId, String authority) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim(AUTH_CLAIM, authority)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMillis)))
                .signWith(key)
                .compact();
    }

    private String createRefreshToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpirationMillis)))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String resolveAuthority(String userId) {
        if ("admin".equals(userId)) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }

    private byte[] decodeSecretKey(String secret) {
        if (secret != null && secret.matches("^[0-9a-fA-F]+$") && secret.length() % 2 == 0) {
            byte[] bytes = new byte[secret.length() / 2];
            for (int i = 0; i < secret.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(secret.substring(i, i + 2), 16);
            }
            return bytes;
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
