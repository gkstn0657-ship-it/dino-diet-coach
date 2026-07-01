package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionToolTest {

    @Mock DietMapper dietMapper;

    private static final int MNO = 7;
    private NutritionTool tool;

    @BeforeEach
    void setUp() {
        tool = new NutritionTool(dietMapper, MNO);
    }

    private Diet diet(String meal, int kcal, int p, int c, int f) {
        Diet d = new Diet();
        d.setMeal(meal);
        d.setTotalKcal(kcal);
        d.setProtein(p);
        d.setCarbs(c);
        d.setFat(f);
        d.setEatenDate(LocalDate.now());
        return d;
    }

    @Test
    @DisplayName("오늘 영양 요약: kcal/단백질/탄수/지방 합계와 끼니 기록 상태를 반환한다")
    void getTodayNutritionSummary_returnsTotalsAndMealStatus() {
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet("breakfast", 400, 20, 50, 10), diet("lunch", 600, 30, 70, 15)));

        String out = tool.getTodayNutritionSummary();

        assertThat(out).contains("총 섭취: 1000 / 2000kcal");
        assertThat(out).contains("목표(2000kcal) 대비 남은: 1000kcal");
        assertThat(out).contains("단백질: 50g / 60g");
        assertThat(out).contains("탄수화물: 120g / 250g");
        assertThat(out).contains("지방: 25g / 60g");
        assertThat(out).contains("아침 완료").contains("점심 완료")
                .contains("저녁 미기록").contains("간식 미기록");
    }

    @Test
    @DisplayName("오늘 영양 요약은 항상 로그인한 사용자(mno)만 조회한다")
    void getTodayNutritionSummary_queriesOnlyOwnMno() {
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString())).thenReturn(List.of());

        tool.getTodayNutritionSummary();

        verify(dietMapper).findByMember(eq(MNO), isNull(), anyString());
    }

    @Test
    @DisplayName("선택 날짜 요약: 잘못된 날짜 형식이면 형식 오류를 반환한다")
    void getNutritionSummaryByDate_invalidDate() {
        String out = tool.getNutritionSummaryByDate("2026/06/21");
        assertThat(out).contains("날짜 형식이 올바르지 않습니다");
        verify(dietMapper, never()).findByMember(anyInt(), any(), any());
    }

    @Test
    @DisplayName("선택 날짜 요약: 올바른 날짜는 본인 데이터를 그 날짜로 조회한다")
    void getNutritionSummaryByDate_validDate() {
        when(dietMapper.findByMember(eq(MNO), isNull(), eq("2026-06-21")))
                .thenReturn(List.of(diet("dinner", 500, 25, 40, 12)));

        String out = tool.getNutritionSummaryByDate("2026-06-21");

        assertThat(out).contains("[2026-06-21 영양 요약]");
        assertThat(out).contains("총 섭취: 500 / 2000kcal");
        verify(dietMapper).findByMember(eq(MNO), isNull(), eq("2026-06-21"));
    }
}
