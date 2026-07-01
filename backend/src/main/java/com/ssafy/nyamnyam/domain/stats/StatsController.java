package com.ssafy.nyamnyam.domain.stats;

import com.ssafy.nyamnyam.common.ApiResponse;
import com.ssafy.nyamnyam.security.LoginMember;
import com.ssafy.nyamnyam.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 배치 수동 실행 (date 미지정 시 오늘) — 데모/테스트용 */
    @PostMapping("/run")
    public ApiResponse<Map<String, Object>> run(@RequestParam(required = false) String date) {
        LocalDate target = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        int count = statsService.aggregate(target);
        return ApiResponse.ok("배치 실행 완료", Map.of("date", target.toString(), "count", count));
    }

    /** 내 일일 영양 통계 (최근 14일) */
    @GetMapping("/me")
    public ApiResponse<List<DailyStat>> myStats(@LoginMember LoginUser user) {
        return ApiResponse.ok(statsService.myStats(user.email()));
    }

    /** 기간별 일별 칼로리/매크로 추이 (from~to, yyyy-MM-dd) — diets 원본 직접 집계 */
    @GetMapping("/me/range")
    public ApiResponse<List<DailyStat>> range(@LoginMember LoginUser user,
                                              @RequestParam String from,
                                              @RequestParam String to) {
        return ApiResponse.ok(statsService.range(user.email(), from, to));
    }
}
