package com.ssafy.nyamnyam.security;

import com.ssafy.nyamnyam.common.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String ATTR_LOGIN_USER = "loginUser";
    private final JwtProvider jwtProvider;

    /** 인증 없이 허용되는 (METHOD path-prefix) 목록 */
    private static final List<String[]> PUBLIC = List.of(
            new String[]{"POST", "/api/v1/auth/login"},
            new String[]{"POST", "/api/v1/auth/refresh"},
            new String[]{"POST", "/api/v1/auth/logout"},
            new String[]{"POST", "/api/v1/auth/password/reset-request"}, // 비밀번호 재설정 요청
            new String[]{"POST", "/api/v1/auth/password/reset"},         // 비밀번호 재설정 실행
            new String[]{"POST", "/api/v1/members"},             // 회원가입
            new String[]{"GET", "/api/v1/members/checkEmail"},   // 이메일 중복확인
            new String[]{"GET", "/api/v1/foods"},                // 음식 검색
            new String[]{"POST", "/api/v1/ai/guide"}             // 가이드 챗봇(비로그인 허용)
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight 통과
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        if (isPublic(request)) return true;

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw CustomException.unauthorized("NO_TOKEN");
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtProvider.parse(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            request.setAttribute(ATTR_LOGIN_USER, new LoginUser(email, role));
            return true;
        } catch (ExpiredJwtException e) {
            // 프론트가 이 메시지를 보고 refresh 토큰으로 재발급 시도
            throw CustomException.unauthorized("TOKEN_ERROR");
        } catch (Exception e) {
            throw CustomException.unauthorized("INVALID_TOKEN");
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        for (String[] p : PUBLIC) {
            if (p[0].equals(method) && uri.equals(p[1])) return true;
        }
        // 음식 검색 등 prefix 허용
        if ("GET".equals(method) && uri.startsWith("/api/v1/foods")) return true;
        return false;
    }
}
