package com.ssafy.nyamnyam.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 비밀번호 해시 단위 테스트 (F106/F110) — BCrypt 전용 */
class PasswordEncoderTest {

    private final PasswordEncoder encoder = new PasswordEncoder();

    @Test
    @DisplayName("encode는 BCrypt 해시($2...)를 생성하고 salt로 매번 달라진다")
    void encodeProducesBcryptAndIsSalted() {
        String h1 = encoder.encode("1234");
        String h2 = encoder.encode("1234");

        assertThat(h1).startsWith("$2");          // BCrypt 식별자
        assertThat(h1).isNotEqualTo(h2);          // salt 로 매번 다른 해시
    }

    @Test
    @DisplayName("matches는 올바른 비밀번호만 통과시킨다")
    void matchesValidatesPassword() {
        String encoded = encoder.encode("my-password");

        assertThat(encoder.matches("my-password", encoded)).isTrue();
        assertThat(encoder.matches("wrong-password", encoded)).isFalse();
    }

    @Test
    @DisplayName("matches는 null 입력에 대해 안전하게 false를 반환한다")
    void matchesHandlesNull() {
        assertThat(encoder.matches(null, encoder.encode("x"))).isFalse();
        assertThat(encoder.matches("x", null)).isFalse();
    }
}
