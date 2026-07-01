package com.ssafy.nyamnyam.domain.diet;

import com.ssafy.nyamnyam.common.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 식단 기록 정책 검증:
 * - 같은 날짜+끼니 단일 기록 (중복 생성 차단 + 기존 dno 전달)
 * - 제목 자동 생성 (음식 기준 / 끼니 기준)
 * - 영양 산정 (음식 합산 / kcal 기반 추정)
 * - 식단 저장/수정은 챌린지 인증과 분리
 */
@ExtendWith(MockitoExtension.class)
class DietServiceTest {

    @Mock DietMapper dietMapper;

    private DietService service;

    private static final int MNO = 10;
    private static final LocalDate DAY = LocalDate.of(2026, 6, 22);

    @BeforeEach
    void setUp() {
        service = new DietService(dietMapper);
    }

    private DietFood food(String name, int kcal, int p, int c, int f) {
        DietFood d = new DietFood();
        d.setName(name);
        d.setKcal(kcal);
        d.setProtein(p);
        d.setCarbs(c);
        d.setFat(f);
        return d;
    }

    private Diet capturedInsert() {
        ArgumentCaptor<Diet> cap = ArgumentCaptor.forClass(Diet.class);
        verify(dietMapper).insert(cap.capture());
        return cap.getValue();
    }

    // ===== 같은 날짜+끼니 단일 기록 =====

    @Test
    @DisplayName("같은 날짜+끼니 기록이 이미 있으면 새로 만들지 않고 막는다")
    void create_blocksDuplicateSlot() {
        Diet existing = new Diet();
        existing.setDno(99);
        when(dietMapper.findByMemberAndDateAndMeal(eq(MNO), eq("2026-06-22"), eq("lunch")))
                .thenReturn(existing);

        assertThatThrownBy(() -> service.create(MNO, null, "lunch", DAY, 500, null, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("점심");

        verify(dietMapper, never()).insert(any());
        verify(dietMapper, never()).insertFood(any());
    }

    @Test
    @DisplayName("중복 시 409 + 기존 dno 를 payload 로 함께 내려준다 (수정 화면 유도용)")
    void create_duplicateCarriesExistingDno() {
        Diet existing = new Diet();
        existing.setDno(77);
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString()))
                .thenReturn(existing);

        Throwable thrown = catchThrowable(() -> service.create(MNO, null, "lunch", DAY, 500, null, null));

        assertThat(thrown).isInstanceOf(CustomException.class);
        CustomException ex = (CustomException) thrown;
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getPayload()).isEqualTo(77);
    }

    // ===== 제목 자동 생성 =====

    @Test
    @DisplayName("제목 미입력 + 음식 1개 → 음식명이 제목이 된다")
    void create_autoTitle_singleFood() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, "   ", "lunch", DAY, null, null, List.of(food("닭가슴살", 165, 31, 0, 4)));

        assertThat(capturedInsert().getTitle()).isEqualTo("닭가슴살");
    }

    @Test
    @DisplayName("제목 미입력 + 음식 여러 개 → '대표음식 외 N개'")
    void create_autoTitle_multiFood() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, null, "dinner", DAY, null, null,
                List.of(food("현미밥", 310, 6, 65, 2), food("닭가슴살", 165, 31, 0, 4), food("계란", 80, 7, 1, 5)));

        assertThat(capturedInsert().getTitle()).isEqualTo("현미밥 외 2개");
    }

    @Test
    @DisplayName("제목 미입력 + 음식 없이 kcal만 → '아침 식단' 처럼 끼니 기준 자동 생성")
    void create_autoTitle_kcalOnly() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, null, "breakfast", DAY, 400, null, null);

        assertThat(capturedInsert().getTitle()).isEqualTo("아침 식단");
    }

    @Test
    @DisplayName("제목을 직접 입력하면 그대로 사용한다")
    void create_keepsExplicitTitle() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, "치팅데이 🍔", "lunch", DAY, 900, null, null);

        assertThat(capturedInsert().getTitle()).isEqualTo("치팅데이 🍔");
    }

    // ===== 영양 산정 =====

    @Test
    @DisplayName("음식 목록이 있으면 kcal·탄단지를 음식 합계로 저장한다")
    void create_macroSumFromFoods() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, null, "lunch", DAY, null, null,
                List.of(food("a", 100, 10, 20, 3), food("b", 200, 15, 30, 5)));

        Diet d = capturedInsert();
        assertThat(d.getTotalKcal()).isEqualTo(300);
        assertThat(d.getProtein()).isEqualTo(25);
        assertThat(d.getCarbs()).isEqualTo(50);
        assertThat(d.getFat()).isEqualTo(8);
    }

    @Test
    @DisplayName("음식 없이 kcal만 입력하면 기존 추정 매크로(P20%/C50%/F30%)를 유지한다")
    void create_kcalOnly_estimatedMacros() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, null, "lunch", DAY, 1000, null, null);

        Diet d = capturedInsert();
        assertThat(d.getTotalKcal()).isEqualTo(1000);
        assertThat(d.getProtein()).isEqualTo((int) Math.round(1000 * 0.2 / 4)); // 50
        assertThat(d.getCarbs()).isEqualTo((int) Math.round(1000 * 0.5 / 4));   // 125
        assertThat(d.getFat()).isEqualTo((int) Math.round(1000 * 0.3 / 9));     // 33
    }

    // ===== 챌린지 인증과 분리 =====

    @Test
    @DisplayName("식단 저장은 챌린지 인증을 트리거하지 않는다 (DietMapper 외 협력자 없음)")
    void create_doesNotTriggerChallengeCheckIn() {
        when(dietMapper.findByMemberAndDateAndMeal(anyInt(), anyString(), anyString())).thenReturn(null);

        service.create(MNO, "점심", "lunch", DAY, 170, null, null);

        verify(dietMapper).findByMemberAndDateAndMeal(anyInt(), anyString(), anyString());
        verify(dietMapper).insert(any(Diet.class));
        verify(dietMapper).insertFood(any(DietFood.class));
        verifyNoMoreInteractions(dietMapper);
    }

    @Test
    @DisplayName("식단 수정도 챌린지 인증을 트리거하지 않는다")
    void update_doesNotTriggerChallengeCheckIn() {
        service.update(1, "저녁", "dinner", DAY.toString(), 1700, List.of(new DietFood()));

        verify(dietMapper).update(any(Diet.class));
        verify(dietMapper).deleteFoods(1);
        verify(dietMapper).insertFood(any(DietFood.class));
        verifyNoMoreInteractions(dietMapper);
    }
}
