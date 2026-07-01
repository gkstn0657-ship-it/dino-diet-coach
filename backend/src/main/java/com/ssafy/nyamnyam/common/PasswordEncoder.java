package com.ssafy.nyamnyam.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 해싱 — BCrypt 전용.
 * 해시는 salt 가 포함되어 같은 입력도 매번 다른 결과가 나온다.
 */
@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    /** 비밀번호를 BCrypt 로 해싱 */
    public String encode(String raw) {
        return bcrypt.encode(raw);
    }

    /** 입력 비밀번호가 저장된 BCrypt 해시와 일치하는지 검증 */
    public boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null) return false;
        return bcrypt.matches(raw, encoded);
    }
}
