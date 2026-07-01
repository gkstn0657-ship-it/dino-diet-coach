package com.ssafy.nyamnyam.security;

/** 인증된 사용자 정보 (인터셉터가 request에 심고, @LoginMember로 주입) */
public record LoginUser(String email, String role) {
}
