package com.ssafy.nyamnyam.domain.food;

import com.ssafy.nyamnyam.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodMapper foodMapper;

    /**
     * 음식 검색 — 로컬 DB 만 조회한다(매 검색마다 외부 API 호출하지 않음).
     * 로컬 foods 는 매월 1일 00:00 정기 갱신 / 운영자 수동 갱신으로 식약처 API 에서 채워진다(FoodSyncService).
     */
    @GetMapping
    public ApiResponse<List<Food>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(foodMapper.search(keyword));
    }
}

