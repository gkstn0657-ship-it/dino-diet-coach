package com.ssafy.nyamnyam.domain.member;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** 회원가입 (F106) */
    @PostMapping
    public ApiResponse<Integer> signup(@RequestBody Member member) {
        return ApiResponse.ok("회원가입 완료", memberService.signup(member));
    }

    /** 이메일 중복 확인 */
    @GetMapping("/checkEmail")
    public ApiResponse<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("canUse", memberService.canUseEmail(email));
        return ApiResponse.ok(payload);
    }

    /** 회원 목록 (운영자 전용 — 일반 사용자는 팔로우/프로필로 회원 탐색) */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(@LoginMember LoginUser user,
                                                 @RequestParam(required = false) String keyword) {
        if (user == null || !com.ssafy.nyamnyam.common.Roles.isOperator(user.role())) {
            throw com.ssafy.nyamnyam.common.CustomException.forbidden("회원 목록은 운영자만 볼 수 있어요.");
        }
        List<Member> members = memberService.list(keyword);
        Map<String, Object> payload = new HashMap<>();
        payload.put("list", members);
        payload.put("currentPage", 1);
        payload.put("totalCount", members.size());
        return ApiResponse.ok(payload);
    }

    /** 회원 상세 조회 (F107) */
    @GetMapping("/{email}")
    public ApiResponse<Member> detail(@PathVariable String email) {
        return ApiResponse.ok(memberService.getByEmail(email));
    }

    /** 회원 정보 수정 (F108) - 이름/목표/권한 */
    @PutMapping
    public ApiResponse<Void> update(@RequestBody Member member) {
        memberService.updateInfo(member);
        return ApiResponse.ok("수정 완료", null);
    }

    /** 프로필(키/몸무게/질환) 수정 (F108) */
    @PatchMapping("/{email}/profile")
    public ApiResponse<Void> updateProfile(@PathVariable String email, @RequestBody Member profile) {
        memberService.updateProfile(email, profile);
        return ApiResponse.ok("프로필 수정 완료", null);
    }

    /** 회원 탈퇴 (F109) */
    @DeleteMapping("/{mno}")
    public ApiResponse<Void> delete(@PathVariable Integer mno, @LoginMember LoginUser loginUser) {
        memberService.delete(mno);
        return ApiResponse.ok("탈퇴 완료", null);
    }

    public record PasswordChangeRequest(String currentPassword, String newPassword, String newPasswordConfirm) {}

    /** 로그인 사용자 본인 비밀번호 변경 — 토큰의 email 기준이라 타인 변경 불가 */
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(@LoginMember LoginUser user,
                                            @RequestBody PasswordChangeRequest req) {
        memberService.changePassword(user.email(), req.currentPassword(),
                req.newPassword(), req.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }

    /** 내 개인 목표 칼로리 (프로필 기반 계산) — { targetKcal, bmr, tdee, hasProfile } */
    @GetMapping("/me/target")
    public ApiResponse<Map<String, Object>> myTarget(@LoginMember LoginUser user) {
        return ApiResponse.ok(memberService.targetInfo(user.email()));
    }
}
