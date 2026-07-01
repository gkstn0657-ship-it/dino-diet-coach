package com.ssafy.nyamnyam.domain.member;

import lombok.Data;

import java.time.LocalDateTime;

/** 비밀번호 재설정 토큰 (이메일 기반 재설정 흐름) */
@Data
public class PasswordResetToken {
    private Integer id;
    private Integer mno;
    private String token;
    private LocalDateTime expiresAt;
    private Boolean used;
    private LocalDateTime createdAt;
}
