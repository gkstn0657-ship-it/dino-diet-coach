package com.ssafy.nyamnyam.domain.member;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public record LoginRequest(String email, String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record ResetRequest(String email) {}
    public record ResetConfirm(String token, String newPassword, String newPasswordConfirm) {}

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Member m = memberService.authenticate(req.email(), req.password());
        String access = jwtProvider.createAccessToken(m.getEmail(), m.getRole());
        String refresh = jwtProvider.createRefreshToken(m.getEmail());

        Map<String, Object> payload = new HashMap<>();
        payload.put("accessToken", access);
        payload.put("refreshToken", refresh);
        payload.put("user", m);
        return ApiResponse.ok("로그인 성공", payload);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody RefreshRequest req) {
        try {
            Claims claims = jwtProvider.parse(req.refreshToken());
            String email = claims.getSubject();
            Member m = memberService.getByEmail(email);
            String access = jwtProvider.createAccessToken(m.getEmail(), m.getRole());
            Map<String, Object> payload = new HashMap<>();
            payload.put("accessToken", access);
            return ApiResponse.ok("재발급 성공", payload);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw CustomException.unauthorized("REFRESH_EXPIRED");
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // 토큰은 클라이언트가 폐기. (서버 블랙리스트는 생략)
        return ApiResponse.ok("로그아웃", null);
    }

    /** 비밀번호 재설정 요청 — 계정 존재 여부를 노출하지 않도록 항상 동일한 응답 */
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@RequestBody ResetRequest req) {
        memberService.requestPasswordReset(req.email());
        return ApiResponse.ok("입력하신 이메일이 가입되어 있다면 재설정 안내를 보냈습니다.", null);
    }

    /** 비밀번호 재설정 실행 — 토큰 검증 후 새 비밀번호 저장 */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody ResetConfirm req) {
        memberService.resetPassword(req.token(), req.newPassword(), req.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해 주세요.", null);
    }
}
