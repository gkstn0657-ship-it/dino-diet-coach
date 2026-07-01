package com.ssafy.nyamnyam.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessValidity,
            @Value("${jwt.refresh-token-validity}") long refreshValidity) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }

    public String createAccessToken(String email, String role) {
        return build(email, role, accessValidity);
    }

    public String createRefreshToken(String email) {
        return build(email, null, refreshValidity);
    }

    private String build(String email, String role, long validity) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity));
        if (role != null) builder.claim("role", role);
        return builder.signWith(key).compact();
    }

    /** 유효하면 Claims 반환, 만료/위조면 예외 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
