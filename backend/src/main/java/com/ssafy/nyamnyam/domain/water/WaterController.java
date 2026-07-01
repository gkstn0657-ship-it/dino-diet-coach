package com.ssafy.nyamnyam.domain.water;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.domain.member.MemberService;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 물 섭취 기록 API (메인 물섭취 위젯이 서버에 저장·복원) */
@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterService waterService;
    private final MemberService memberService;

    private Integer mno(LoginUser user) {
        return memberService.getByEmail(user.email()).getMno();
    }

    /** 오늘 물 섭취 잔 수 */
    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> today(@LoginMember LoginUser user) {
        return ApiResponse.ok(Map.of("cups", waterService.getToday(mno(user))));
    }

    public record WaterRequest(int cups) {}

    /** 오늘 물 섭취 잔 수 저장(덮어쓰기) */
    @PutMapping("/today")
    public ApiResponse<Map<String, Object>> setToday(@LoginMember LoginUser user,
                                                     @RequestBody WaterRequest req) {
        return ApiResponse.ok(Map.of("cups", waterService.setToday(mno(user), req.cups())));
    }
}
