																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																							package com.ssafy.nyamnyam.domain.challenge;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.common.FileStorage;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final FileStorage fileStorage;

    private static final java.util.Set<String> OPERATOR_ROLES = com.ssafy.nyamnyam.common.Roles.OPERATOR_ROLES;

    private Integer mno(LoginUser user) {
        return memberService.getByEmail(user.email()).getMno();
    }

    /** 챌린지 목록 (F112) — 공개(승인+노출)만. status=recruiting|live|ended|all, sort=popular|latest */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @LoginMember LoginUser user,
            @RequestParam(required = false, defaultValue = "recruiting") String status,
            @RequestParam(required = false, defaultValue = "popular") String sort) {
        return ApiResponse.ok(challengeService.list(mno(user), status, sort));
    }

    /** 내 챌린지 (F113) — '/{cno}' 보다 먼저 매핑되도록 위에 둠 */
    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> my(@LoginMember LoginUser user) {
        return ApiResponse.ok(challengeService.myChallenges(mno(user)));
    }

    /** 챌린지 상세 (F112/F113) */
    @GetMapping("/{cno}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Integer cno, @LoginMember LoginUser user) {
        return ApiResponse.ok(challengeService.detail(cno, mno(user)));
    }

    /** 챌린지 생성 (F112) - 이미지 멀티파트 + 식단 인증 조건(선택) */
    @PostMapping
    public ApiResponse<Integer> create(
            @LoginMember LoginUser user,
            @RequestParam String title,
            @RequestParam(required = false) String desc,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String condType,
            @RequestParam(required = false) Integer condValue,
            @RequestParam(required = false) MultipartFile image) {
        String imageUrl = fileStorage.save(image);
        Integer cno = challengeService.create(mno(user), title, desc, startDate, endDate, imageUrl, condType, condValue);
        return ApiResponse.ok("챌린지 등록을 신청했어요. 운영자 승인 후 공개됩니다.", cno);
    }

    /** 챌린지 삭제 — 운영자만(참가자 있으면 숨김 처리). 일반 사용자는 삭제 불가 */
    @DeleteMapping("/{cno}")
    public ApiResponse<Void> delete(@PathVariable Integer cno, @LoginMember LoginUser user) {
        if (user == null || user.role() == null || !OPERATOR_ROLES.contains(user.role())) {
            throw com.ssafy.nyamnyam.common.CustomException.forbidden("챌린지 삭제는 운영자만 가능합니다.");
        }
        challengeService.opsDelete(cno);
        return ApiResponse.ok("처리 완료", null);
    }

    /** 챌린지 참여 (F113) — 모집중에만 참여 가능 */
    @PostMapping("/{cno}/join")
    public ApiResponse<Void> join(@PathVariable Integer cno, @LoginMember LoginUser user) {
        challengeService.join(cno, mno(user));
        return ApiResponse.ok("참여 완료", null);
    }

    /** 챌린지 참여 취소 — 모집중(시작 전)에만 */
    @DeleteMapping("/{cno}/join")
    public ApiResponse<Void> leave(@PathVariable Integer cno, @LoginMember LoginUser user) {
        challengeService.leave(cno, mno(user));
        return ApiResponse.ok("참여를 취소했어요.", null);
    }

    /** 챌린지 오늘 인증 (F113) — 달성도 증가 */
    @PostMapping("/{cno}/check")
    public ApiResponse<Map<String, Object>> check(@PathVariable Integer cno, @LoginMember LoginUser user) {
        return ApiResponse.ok("인증 완료", challengeService.checkIn(cno, mno(user)));
    }

    /** [개발·시연용] 자동 인증 마감 배치 즉시 실행 — 23:50을 기다리지 않고 검증 */
    @PostMapping("/auto-check-run")
    public ApiResponse<Integer> autoCheckRun(@LoginMember LoginUser user) {
        int recorded = challengeService.autoCheckAllParticipants();
        return ApiResponse.ok("자동 인증 배치 실행 완료", recorded);
    }
}
