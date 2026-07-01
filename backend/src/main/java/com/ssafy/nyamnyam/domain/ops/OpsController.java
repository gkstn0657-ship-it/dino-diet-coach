package com.ssafy.nyamnyam.domain.ops;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.domain.member.Member;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 운영 콘솔 API.
 * 모든 엔드포인트는 서버에서 운영 권한을 검사한다(프론트 가드만 신뢰하지 않음).
 * 운영 권한 = OPERATOR (신규) 또는 ADMIN (기존 호환). 그 외에는 403.
 */
@RestController
@RequestMapping("/api/v1/ops")
@RequiredArgsConstructor
public class OpsController {

    /** 운영 권한 role 집합 — 공통 정의(Roles) 참조 */
    private static final Set<String> OPERATOR_ROLES = com.ssafy.nyamnyam.common.Roles.OPERATOR_ROLES;

    private final OpsService opsService;

    /** 운영 권한이 아니면 403 차단 */
    private void requireOperator(LoginUser user) {
        if (user == null || user.role() == null || !OPERATOR_ROLES.contains(user.role())) {
            throw CustomException.forbidden("운영 콘솔 접근 권한이 없습니다.");
        }
    }

    /** 운영 요약 통계 */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@LoginMember LoginUser user) {
        requireOperator(user);
        return ApiResponse.ok(opsService.summary());
    }

    /** 회원 운영 목록 (검색) */
    @GetMapping("/members")
    public ApiResponse<List<Member>> members(@LoginMember LoginUser user,
                                             @RequestParam(required = false) String keyword) {
        requireOperator(user);
        return ApiResponse.ok(opsService.members(keyword));
    }

    /** 챌린지 운영 목록 */
    @GetMapping("/challenges")
    public ApiResponse<List<Map<String, Object>>> challenges(@LoginMember LoginUser user) {
        requireOperator(user);
        return ApiResponse.ok(opsService.challenges());
    }

    /** 게시글 운영 목록 (숨김 포함) */
    @GetMapping("/posts")
    public ApiResponse<List<Map<String, Object>>> posts(@LoginMember LoginUser user) {
        requireOperator(user);
        return ApiResponse.ok(opsService.posts());
    }

    public record HiddenRequest(boolean hidden) {}
    public record ActiveRequest(boolean active) {}

    /** 게시글 숨김/해제 */
    @PatchMapping("/posts/{bno}/hidden")
    public ApiResponse<Void> setPostHidden(@LoginMember LoginUser user,
                                           @PathVariable Integer bno,
                                           @RequestBody HiddenRequest req) {
        requireOperator(user);
        opsService.setPostHidden(bno, req.hidden());
        return ApiResponse.ok(req.hidden() ? "숨김 처리 완료" : "숨김 해제 완료", null);
    }

    /** 회원 정지/해제 */
    @PatchMapping("/members/{mno}/active")
    public ApiResponse<Void> setMemberActive(@LoginMember LoginUser user,
                                             @PathVariable Integer mno,
                                             @RequestBody ActiveRequest req) {
        requireOperator(user);
        opsService.setMemberActive(mno, req.active());
        return ApiResponse.ok(req.active() ? "정지 해제 완료" : "정지 처리 완료", null);
    }

    /** 식품 DB 수동 갱신(식약처 API → 로컬 foods) */
    @PostMapping("/foods/sync")
    public ApiResponse<Map<String, Object>> syncFoods(@LoginMember LoginUser user) {
        requireOperator(user);
        return ApiResponse.ok("식품 갱신 완료", opsService.syncFoods());
    }

    public record ApprovalRequest(String status) {}
    public record VisibilityRequest(boolean visible) {}

    /** 챌린지 승인/거부 (status: APPROVED | REJECTED | PENDING) */
    @PatchMapping("/challenges/{cno}/approval")
    public ApiResponse<Void> setChallengeApproval(@LoginMember LoginUser user,
                                                  @PathVariable Integer cno,
                                                  @RequestBody ApprovalRequest req) {
        requireOperator(user);
        opsService.setChallengeApproval(cno, req.status());
        return ApiResponse.ok("처리 완료", null);
    }

    /** 챌린지 숨김/노출 */
    @PatchMapping("/challenges/{cno}/visibility")
    public ApiResponse<Void> setChallengeVisibility(@LoginMember LoginUser user,
                                                    @PathVariable Integer cno,
                                                    @RequestBody VisibilityRequest req) {
        requireOperator(user);
        opsService.setChallengeVisibility(cno, req.visible());
        return ApiResponse.ok(req.visible() ? "노출 처리 완료" : "숨김 처리 완료", null);
    }

    /** 챌린지 삭제(참가자 있으면 숨김 처리) */
    @DeleteMapping("/challenges/{cno}")
    public ApiResponse<Void> deleteChallenge(@LoginMember LoginUser user, @PathVariable Integer cno) {
        requireOperator(user);
        return ApiResponse.ok(opsService.deleteChallenge(cno), null);
    }
}
