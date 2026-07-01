package com.ssafy.nyamnyam.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** JWT 발급·검증 단위 테스트 (F110) */
class JwtProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-unit-test-256bit-1234567890";

    private final JwtProvider provider = new JwtProvider(SECRET, 60_000L, 120_000L);

    @Test
    @DisplayName("액세스 토큰을 발급하고 이메일·역할을 복원할 수 있다")
    void createAndParseAccessToken() {
        String token = provider.createAccessToken("user@ssafy.com", "USER");

        Claims claims = provider.parse(token);
        assertThat(claims.getSubject()).isEqualTo("user@ssafy.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("리프레시 토큰에는 role 클레임이 없다")
    void refreshTokenHasNoRole() {
        String token = provider.createRefreshToken("user@ssafy.com");

        Claims claims = provider.parse(token);
        assertThat(claims.getSubject()).isEqualTo("user@ssafy.com");
        assertThat(claims.get("role")).isNull();
    }

    @Test
    @DisplayName("만료된 토큰은 ExpiredJwtException을 던진다")
    void expiredTokenThrows() {
        JwtProvider shortLived = new JwtProvider(SECRET, -1_000L, -1_000L); // 즉시 만료
        String token = shortLived.createAccessToken("user@ssafy.com", "USER");

        assertThatThrownBy(() -> shortLived.parse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("다른 키로 서명된(위조) 토큰은 검증에 실패한다")
    void tamperedTokenThrows() {
        JwtProvider attacker = new JwtProvider("attacker-key-attacker-key-attacker-key-12345", 60_000L, 60_000L);
        String forged = attacker.createAccessToken("hacker@evil.com", "ADMIN");

        assertThatThrownBy(() -> provider.parse(forged))
                .isInstanceOf(JwtException.class);
    }
}
