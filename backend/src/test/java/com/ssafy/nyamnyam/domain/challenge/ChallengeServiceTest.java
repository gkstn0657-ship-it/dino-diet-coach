package com.ssafy.nyamnyam.domain.challenge;

import com.ssafy.nyamnyam.common.CustomException;
import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import com.ssafy.nyamnyam.domain.water.WaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 식단 연동 인증 조건 게이트 검증.
 * "조건을 충족한 사람만 일일 인증이 되는가"를 서버 로직 수준에서 증명한다.
 */
@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock ChallengeMapper challengeMapper;
    @Mock DietMapper dietMapper;
    @Mock WaterService waterService;

    private ChallengeService service;

    private static final int CNO = 1;
    private static final int MNO = 10;

    @BeforeEach
    void setUp() {
        service = new ChallengeService(challengeMapper, dietMapper, waterService);
    }

    private Challenge challenge(String condType, Integer condValue) {
        Challenge c = new Challenge();
        c.setCno(CNO);
        c.setMno(99); // 생성자는 다른 사람
        c.setTitle("1500kcal 클린식단");
        c.setStartDate(LocalDate.now().minusDays(1));
        c.setEndDate(LocalDate.now().plusDays(7));
        c.setCondType(condType);
        c.setCondValue(condValue);
        return c;
    }

    private ChallengeParticipant participant() {
        ChallengeParticipant p = new ChallengeParticipant();
        p.setCno(CNO);
        p.setMno(MNO);
        p.setDoneDays(0);
        p.setProgress(0);
        p.setTotalDays(7);
        p.setLastCheckDate(null); // 오늘 미인증
        return p;
    }

    private Diet diet(int kcal, int protein) {
        Diet d = new Diet();
        d.setTotalKcal(kcal);
        d.setProtein(protein);
        d.setEatenDate(LocalDate.now());
        return d;
    }

    // ===== 식단 조건 챌린지: 수동 인증 차단 (하루 마감 배치로만 인증) =====

    @Test
    @DisplayName("식단 조건 챌린지는 현재 총합이 조건을 만족해도 수동 인증을 막는다 (마감 배치 전용)")
    void checkIn_blockedForConditionChallenge_evenWhenCurrentlyMet() {
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));

        assertThatThrownBy(() -> service.checkIn(CNO, MNO))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("하루 마감");

        // 수동 인증으로는 상태가 절대 바뀌지 않는다 (식단 조회도 하지 않음)
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
        verifyNoInteractions(dietMapper);
    }

    @Test
    @DisplayName("단백질 조건 챌린지도 동일하게 수동 인증이 막힌다")
    void checkIn_blockedForProteinConditionChallenge() {
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_PROTEIN_MIN", 100));

        assertThatThrownBy(() -> service.checkIn(CNO, MNO))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("하루 마감");

        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    // ===== 조건 없는 챌린지 / 참여 정책 =====

    @Test
    @DisplayName("조건 없는 챌린지는 식단과 무관하게 버튼 인증이 된다 (기존 방식 유지)")
    void checkIn_noConditionWorksAsBefore() {
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(challengeMapper.findById(CNO)).thenReturn(challenge(null, null));

        Map<String, Object> result = service.checkIn(CNO, MNO);

        assertThat(result).containsEntry("doneDays", 1);
        verifyNoInteractions(dietMapper); // 식단 조회 자체를 안 함
    }

    @Test
    @DisplayName("참여(join)는 자유롭게 가능하지만, 참여 직후 인증을 확정하지 않는다")
    void join_isFreeOfCondition_andDoesNotCheckImmediately() {
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(null);
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));

        service.join(CNO, MNO); // 식단 기록 없어도, 조건 미달이어도 참여 OK

        verify(challengeMapper).insertParticipant(any(ChallengeParticipant.class));
        // 참여 시점에는 인증 상태를 절대 변경하지 않는다 (식단 평가도 하지 않음)
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
        verifyNoInteractions(dietMapper);
    }

    // ===== 상세 조회: 인증 상태를 변경하지 않고 현재 충족 여부만 표시 =====

    @Test
    @DisplayName("상세 조회는 현재 조건 충족(condMet)을 표시하되 인증을 확정하지 않는다")
    void detail_showsCondMetButDoesNotCheckIn() {
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(170, 10))); // 점심 170kcal — 현재는 1500 이하
        when(challengeMapper.leaderboard(CNO)).thenReturn(List.of());

        Map<String, Object> result = service.detail(CNO, MNO);

        // 현재 시점 조건 충족 여부는 표시
        assertThat(result).containsEntry("condMet", true);
        assertThat(result).containsEntry("checkedToday", false);
        // 그러나 상세 조회만으로 인증이 확정되면 안 된다
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    // ===== 하루 마감 배치(autoCheckAllParticipants): 인증은 여기서만 확정 =====

    @Test
    @DisplayName("마감 배치: 하루 총합이 조건 충족이면 인증이 기록된다")
    void autoCheckAll_recordsWhenConditionMet() {
        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        when(challengeMapper.findAll()).thenReturn(List.of(c));
        when(challengeMapper.findParticipants(CNO)).thenReturn(List.of(participant()));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1200, 50))); // 하루 총합 1200 ≤ 1500

        int recorded = service.autoCheckAllParticipants();

        assertThat(recorded).isEqualTo(1);
        verify(challengeMapper).updateParticipant(eq(CNO), eq(MNO), eq(1), anyInt(), eq(LocalDate.now()));
    }

    @Test
    @DisplayName("마감 배치: 식단 미기록이면 '이하' 조건은 통과로 치지 않는다 (안 먹은 척 꼼수 차단)")
    void autoCheckAll_skipsWhenNoMealLogged() {
        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        when(challengeMapper.findAll()).thenReturn(List.of(c));
        when(challengeMapper.findParticipants(CNO)).thenReturn(List.of(participant()));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString())).thenReturn(List.of()); // 기록 없음

        int recorded = service.autoCheckAllParticipants();

        assertThat(recorded).isZero();
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("⏰ 날짜가 다음날로 바뀌면 마감 배치(autoCheckAllParticipants) 실행 시 다음 일차로 자동 인증된다")
    void autoCheckAll_recordsNextDayWhenDateChanges() {
        LocalDate day1 = LocalDate.of(2026, 6, 22);
        LocalDate day2 = LocalDate.of(2026, 6, 23);

        // 두 날짜 모두 LIVE 상태가 되도록 기간을 넉넉히 잡는다 (status()는 clock 기준)
        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        c.setStartDate(LocalDate.of(2026, 6, 1));
        c.setEndDate(LocalDate.of(2026, 7, 31));

        ChallengeParticipant p = participant(); // doneDays=0, totalDays=7, lastCheckDate=null

        when(challengeMapper.findAll()).thenReturn(List.of(c));
        when(challengeMapper.findParticipants(CNO)).thenReturn(List.of(p));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);
        // 식단 조회는 전달받은 '날짜 문자열'에 따라 다른 식단을 반환 (실제 SQL의 날짜 필터를 흉내)
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenAnswer(inv -> {
                    String date = inv.getArgument(2);
                    if ("2026-06-22".equals(date)) return List.of(diet(1200, 50)); // 6/22 총합 1200 ≤ 1500
                    if ("2026-06-23".equals(date)) return List.of(diet(1300, 55)); // 6/23 총합 1300 ≤ 1500
                    return List.of();
                });
        // updateParticipant 가 실제 DB처럼 참여자 상태를 갱신하도록 흉내
        when(challengeMapper.updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenAnswer(inv -> {
                    p.setDoneDays(inv.getArgument(2));
                    p.setProgress(inv.getArgument(3));
                    p.setLastCheckDate(inv.getArgument(4));
                    return 1;
                });

        // [6/22] 마감 배치 → 1일차, lastCheckDate=6/22
        setClock(day1);
        service.autoCheckAllParticipants();
        assertThat(p.getDoneDays()).isEqualTo(1);
        assertThat(p.getLastCheckDate()).isEqualTo(day1);

        // [같은 날 재실행] 이미 6/22로 인증됨 → 중복 인증 없음, 여전히 1일차
        service.autoCheckAllParticipants();
        assertThat(p.getDoneDays()).isEqualTo(1);
        verify(challengeMapper, times(1))
                .updateParticipant(eq(CNO), eq(MNO), eq(1), anyInt(), eq(day1));

        // [6/23로 날짜 전환] 마감 배치 → 2일차, lastCheckDate=6/23, 진행률 2/7 기준 갱신
        setClock(day2);
        service.autoCheckAllParticipants();
        assertThat(p.getDoneDays()).isEqualTo(2);
        assertThat(p.getLastCheckDate()).isEqualTo(day2);
        assertThat(p.getProgress()).isEqualTo((int) Math.round(2 * 100.0 / 7)); // 29%
        verify(challengeMapper).updateParticipant(eq(CNO), eq(MNO), eq(2), eq(29), eq(day2));
    }

    // ===== 버그 재현 시나리오 =====

    @Test
    @DisplayName("🐛 점심 170kcal 시점엔 인증되지 않고, 저녁 1700kcal 추가 후 마감 배치에서도 총합 1870>1500이라 인증되지 않는다")
    void scenario_partialSumDoesNotLockInBeforeDayEnd() {
        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        List<Diet> today = new java.util.ArrayList<>();
        today.add(diet(170, 10)); // 점심만 등록된 상태

        when(challengeMapper.findById(CNO)).thenReturn(c);
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(challengeMapper.leaderboard(CNO)).thenReturn(List.of());
        // 식단 조회는 호출 시점의 '현재 총합'을 반환 (실제 DB의 당일 누적을 흉내)
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenAnswer(inv -> List.copyOf(today));

        // 1) 점심 170kcal만 있는 상태에서 상세를 봐도 인증이 확정되면 안 된다
        service.detail(CNO, MNO);
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());

        // 2) 저녁 1700kcal 추가 → 하루 총합 1870kcal (조건 1500 초과)
        today.add(diet(1700, 40));
        when(challengeMapper.findAll()).thenReturn(List.of(c));
        when(challengeMapper.findParticipants(CNO)).thenReturn(List.of(participant()));

        int recorded = service.autoCheckAllParticipants();

        // 마감 배치에서 진짜 총합으로 평가 → 실패이므로 인증되지 않는다
        assertThat(recorded).isZero();
        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    // ===== 자동 인증 (autoCheckIn): 오늘 식단이 조건 충족이면 시스템이 기록 =====

    @Test
    @DisplayName("조건 충족(1200 ≤ 1500) 시 자동 인증이 기록된다")
    void autoCheckIn_recordsWhenConditionMet() {
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1200, 50)));

        service.autoCheckIn(MNO);

        verify(challengeMapper).updateParticipant(eq(CNO), eq(MNO), eq(1), anyInt(), eq(LocalDate.now()));
    }

    @Test
    @DisplayName("조건 미충족(1800 > 1500)이면 자동 인증이 기록되지 않는다")
    void autoCheckIn_skipsWhenConditionNotMet() {
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(participant());
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1800, 50)));

        service.autoCheckIn(MNO);

        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("어제 인증했어도 날짜가 바뀌면(오늘) 다시 자동 인증된다 — 날짜 전환 검증")
    void autoCheckIn_recordsAgainOnNewDay() {
        ChallengeParticipant p = participant();
        p.setLastCheckDate(LocalDate.now().minusDays(1)); // 마지막 인증이 '어제'
        p.setDoneDays(3);
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1200, 50)));

        service.autoCheckIn(MNO);

        // 어제 기록과 무관하게 오늘 날짜로 4일차가 기록된다
        verify(challengeMapper).updateParticipant(eq(CNO), eq(MNO), eq(4), anyInt(), eq(LocalDate.now()));
    }

    /** 서비스의 시계를 특정 날짜로 고정 (시간 여행) */
    private void setClock(LocalDate date) {
        ReflectionTestUtils.setField(service, "clock",
                Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    @Test
    @DisplayName("⏰ 실제 날짜가 하루 지나면 챌린지 일수가 1→2로 늘어난다 (시계 전진 시뮬레이션)")
    void autoCheckIn_daysGrowWhenRealDatePasses() {
        LocalDate day1 = LocalDate.of(2026, 6, 11);

        // 시계와 무관하게 항상 LIVE 상태인 챌린지
        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        c.setStartDate(day1.minusDays(1));
        c.setEndDate(day1.plusDays(30));

        // updateParticipant 가 실제 DB처럼 참여자 상태를 갱신하도록 흉내
        ChallengeParticipant p = participant(); // 0일차에서 시작
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(c);
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1200, 50))); // 매일 조건 충족
        when(challengeMapper.updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenAnswer(inv -> {
                    p.setDoneDays(inv.getArgument(2));
                    p.setProgress(inv.getArgument(3));
                    p.setLastCheckDate(inv.getArgument(4));
                    return 1;
                });

        // [1일차] 6/11 — 자동 인증 → 1일
        setClock(day1);
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(1);
        assertThat(p.getLastCheckDate()).isEqualTo(day1);

        // [같은 날 재실행] — 중복 기록 없음, 여전히 1일
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(1);

        // [날짜 전환] 6/12로 하루 전진 — 자동 인증 → 2일
        setClock(day1.plusDays(1));
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(2);
        assertThat(p.getLastCheckDate()).isEqualTo(day1.plusDays(1));

        // [하루 더] 6/13 — 3일
        setClock(day1.plusDays(2));
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(3);
        assertThat(p.getProgress()).isEqualTo(43); // 3/7일 ≈ 43%
    }

    @Test
    @DisplayName("⏰ 식단을 기록하지 않은 날은 날짜가 지나도 일수가 늘지 않는다")
    void autoCheckIn_skipsDayWithoutDiet() {
        LocalDate day1 = LocalDate.of(2026, 6, 11);

        Challenge c = challenge("DAILY_KCAL_MAX", 1500);
        c.setStartDate(day1.minusDays(1));
        c.setEndDate(day1.plusDays(30));

        ChallengeParticipant p = participant();
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(c);
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);
        when(challengeMapper.updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenAnswer(inv -> {
                    p.setDoneDays(inv.getArgument(2));
                    p.setProgress(inv.getArgument(3));
                    p.setLastCheckDate(inv.getArgument(4));
                    return 1;
                });
        // 날짜별 식단: 6/11과 6/13에만 기록, 6/12는 미기록 — 실제 SQL의 날짜 필터를 흉내
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenAnswer(inv -> {
                    String date = inv.getArgument(2);
                    return date.equals("2026-06-12") ? List.of() : List.of(diet(1200, 50));
                });

        setClock(day1);                 // 6/11: 식단 있음 → 1일
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(1);

        setClock(day1.plusDays(1));     // 6/12: 식단 없음 → 그대로 1일 (핵심 검증)
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(1);
        assertThat(p.getLastCheckDate()).isEqualTo(day1); // 마지막 기록일도 6/11 유지

        setClock(day1.plusDays(2));     // 6/13: 식단 다시 있음 → 2일
        service.autoCheckIn(MNO);
        assertThat(p.getDoneDays()).isEqualTo(2);
    }

    // ===== AI 챌린지 Tool 용 조회 전용 상태 텍스트 =====

    @Test
    @DisplayName("myChallengeStatusText: 참여 챌린지의 진행률·조건·오늘 충족/인증 상태를 반환한다")
    void myChallengeStatusText_returnsProgressAndTodayCondition() {
        ChallengeParticipant p = participant();
        p.setDoneDays(2);
        p.setProgress(29);
        p.setLastCheckDate(null); // 오늘 미인증
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);
        when(dietMapper.findByMember(eq(MNO), isNull(), anyString()))
                .thenReturn(List.of(diet(1200, 50))); // 오늘 1200 ≤ 1500 → 충족

        String text = service.myChallengeStatusText(MNO);

        assertThat(text).contains("1500kcal 클린식단");
        assertThat(text).contains("2/7일");
        assertThat(text).contains("조건:");
        assertThat(text).contains("충족");
        // 서버가 계산한 '챌린지 통과까지' 여유: 1500 - 1200 = 300kcal
        assertThat(text).contains("챌린지 통과까지");
        assertThat(text).contains("인증까지 300kcal 여유");
        assertThat(text).contains("오늘 인증: 아직");
    }

    @Test
    @DisplayName("myChallengeStatusText: 참여 챌린지가 없으면 안내 문구를 반환한다")
    void myChallengeStatusText_empty() {
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of());

        assertThat(service.myChallengeStatusText(MNO)).contains("참여 중인 챌린지가 없습니다");
    }

    @Test
    @DisplayName("오늘 이미 자동 기록됐다면 중복 기록하지 않는다")
    void autoCheckIn_noDuplicatePerDay() {
        ChallengeParticipant p = participant();
        p.setLastCheckDate(LocalDate.now()); // 오늘 이미 인증됨
        when(challengeMapper.findJoinedCnos(MNO)).thenReturn(List.of(CNO));
        when(challengeMapper.findById(CNO)).thenReturn(challenge("DAILY_KCAL_MAX", 1500));
        when(challengeMapper.findParticipant(CNO, MNO)).thenReturn(p);

        service.autoCheckIn(MNO);

        verify(challengeMapper, never()).updateParticipant(anyInt(), anyInt(), anyInt(), anyInt(), any());
        verifyNoInteractions(dietMapper); // 식단 조회 전에 차단
    }
}
