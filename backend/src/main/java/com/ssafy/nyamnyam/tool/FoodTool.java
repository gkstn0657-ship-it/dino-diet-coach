package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.food.Food;
import com.ssafy.nyamnyam.domain.food.FoodApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 음식 영양 조회 Tool (외부 API).
 * 기존 {@link FoodApiService}(식약처 식품영양성분 DB)를 LLM이 호출 가능한 Tool 로 노출한다.
 * Tool 은 책임만 가지고 실제 조회 로직은 Service 에 위임한다. (F1201 / F1203)
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FoodTool {

    private final FoodApiService foodApiService;

    @Tool(description = """
            [역할]
            - 음식 이름으로 식약처 식품영양성분 DB를 조회해 1인분(약 100g) 기준 영양정보를 반환한다.
            [입력]
            - foodName: 조회할 음식 이름 (예: '닭가슴살', '현미밥', '떡볶이')
            [출력]
            - 음식별 칼로리/단백질/탄수화물/지방 (상위 5건)
            [정책]
            - 추측 금지. 반드시 조회 결과만 사용한다.
            - 결과가 없으면 "해당 음식의 영양정보를 찾지 못했습니다." 라고만 답한다.
            """)
    public String searchFoodNutrition(String foodName) {
        log.debug("### TOOL CALLED: searchFoodNutrition, foodName={}", foodName);

        List<Food> foods = foodApiService.search(foodName);
        // FoodApiService 는 외부 API 실패 시 null 을 반환할 수 있으므로 LLM 에 null 이 가지 않도록 방어
        if (foods == null || foods.isEmpty()) {
            return "해당 음식의 영양정보를 찾지 못했습니다: " + foodName;
        }

        StringBuilder sb = new StringBuilder("[" + foodName + " 영양정보 조회 결과]\n");
        foods.stream().limit(5).forEach(f -> sb
                .append("- ").append(f.getName())
                .append(": ").append(nz(f.getKcal())).append("kcal")
                .append(", 단백질 ").append(nz(f.getProtein())).append("g")
                .append(", 탄수화물 ").append(nz(f.getCarbs())).append("g")
                .append(", 지방 ").append(nz(f.getFat())).append("g\n"));
        return sb.toString().trim();
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
