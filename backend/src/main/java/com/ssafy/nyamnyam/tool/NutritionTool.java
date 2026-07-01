package com.ssafy.nyamnyam.tool;

import com.ssafy.nyamnyam.domain.diet.Diet;
import com.ssafy.nyamnyam.domain.diet.DietMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.util.*;

/**
 * 현재 로그인한 회원의 영양 요약 Tool (내부 데이터).
 *
 * 보안 설계: mno 를 LLM 입력이 아닌 생성자 주입으로 받아 타인 데이터 조회를 차단한다.
 * 숫자 합계·끼니 상태·주간 평균 등 '서버가 계산해야 하는 값'을 LLM 대신 정확히 계산해 제공한다.
 */
@Slf4j
public class NutritionTool {

    private static final int DEFAULT_targetKcal = 2000;
    private static final int TARGET_PROTEIN = 60;
    private static final int TARGET_CARBS = 250;
    private static final int TARGET_FAT = 60;

    /** meal key → 라벨 (끼니 상태 표기용) */
    private static final String[][] MEALS = {
            {"breakfast", "아침"}, {"lunch", "점심"}, {"dinner", "저녁"}, {"snack", "간식"}
    };

    private final DietMapper dietMapper;
    private final Integer mno;
    private final int targetKcal;

    /** 기존 호환 생성자 — 기본 목표(2000kcal) */
    public NutritionTool(DietMapper dietMapper, Integer mno) {
        this(dietMapper, mno, DEFAULT_targetKcal);
    }

    /** 개인 목표 칼로리 주입 생성자 */
    public NutritionTool(DietMapper dietMapper, Integer mno, int targetKcal) {
        this.dietMapper = dietMapper;
        this.mno = mno;
        this.targetKcal = targetKcal > 0 ? targetKcal : DEFAULT_targetKcal;
    }

    @Tool(description = """
            [역할]
            - 현재 로그인한 사용자의 '오늘' 영양 요약(총 kcal/목표/남은 kcal, 단백질·탄수·지방, 끼니 기록 상태)을 반환한다.
            [입력]
            - 없음 (항상 본인의 오늘 데이터만 조회)
            [출력]
            - 총 섭취/목표/남은 칼로리, 단백질·탄수화물·지방 합계(목표 대비), 아침/점심/저녁/간식 기록 여부, 끼니 수
            [정책]
            - 식사 조언·하루 상태 요약·챌린지 여유 판단의 1순위 근거로 사용한다.
            """)
    public String getTodayNutritionSummary() {
        log.debug("### TOOL CALLED: getTodayNutritionSummary, mno={}", mno);
        return summarize("오늘", dietMapper.findByMember(mno, null, LocalDate.now().toString()));
    }

    @Tool(description = """
            [역할]
            - 지정한 날짜(yyyy-MM-dd)의 영양 요약을 반환한다. (본인 데이터만)
            [입력]
            - date: 'yyyy-MM-dd' 형식 날짜 문자열 (예: '2026-06-21')
            [출력]
            - 해당 날짜 총 kcal, 단백질·탄수·지방, 끼니 기록, 음식 목록
            [정책]
            - 날짜 형식이 잘못되면 형식 오류를 반환한다. 날짜는 현재 날짜/시간 도구로 먼저 확인한다.
            """)
    public String getNutritionSummaryByDate(String date) {
        log.debug("### TOOL CALLED: getNutritionSummaryByDate, mno={}, date={}", mno, date);
        LocalDate d;
        try {
            d = LocalDate.parse(date.trim());
        } catch (Exception e) {
            return "날짜 형식이 올바르지 않습니다. 'yyyy-MM-dd' 형식으로 입력해주세요. (입력: " + date + ")";
        }
        return summarize(d.toString(), dietMapper.findByMember(mno, null, d.toString()));
    }

    @Tool(description = """
            [역할]
            - 현재 로그인한 사용자의 '최근 7일' 식단 패턴 요약을 반환한다.
            [입력]
            - 없음
            [출력]
            - 7일 평균 kcal/단백질/탄수/지방, 총 기록 끼니 수, 기록이 없는 날짜, 가장 부족/초과한 영양소
            [정책]
            - "요즘 어때?", "이번 주 잘하고 있어?", "내 패턴 봐줘" 같은 질문에 사용한다.
            """)
    public String getWeeklyNutritionSummary() {
        log.debug("### TOOL CALLED: getWeeklyNutritionSummary, mno={}", mno);
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);
        List<Diet> week = dietMapper.findByMember(mno, null, null).stream()
                .filter(x -> x.getEatenDate() != null
                        && !x.getEatenDate().isBefore(from) && !x.getEatenDate().isAfter(today))
                .toList();

        int days = 7;
        int kcal = week.stream().mapToInt(x -> nz(x.getTotalKcal())).sum();
        int protein = week.stream().mapToInt(x -> nz(x.getProtein())).sum();
        int carbs = week.stream().mapToInt(x -> nz(x.getCarbs())).sum();
        int fat = week.stream().mapToInt(x -> nz(x.getFat())).sum();

        // 기록이 없는 날짜
        Set<LocalDate> logged = new HashSet<>();
        week.forEach(x -> logged.add(x.getEatenDate()));
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate dd = from.plusDays(i);
            if (!logged.contains(dd)) missing.add(dd.toString());
        }

        int avgKcal = Math.round(kcal / (float) days);
        int avgP = Math.round(protein / (float) days);
        int avgC = Math.round(carbs / (float) days);
        int avgF = Math.round(fat / (float) days);

        StringBuilder sb = new StringBuilder("[최근 7일 식단 요약]\n");
        if (week.isEmpty()) {
            sb.append("- 최근 7일간 기록된 식단이 없습니다.");
            return sb.toString();
        }
        sb.append("- 기록 끼니 수: ").append(week.size()).append("끼\n");
        sb.append("- 하루 평균: ").append(avgKcal).append("kcal");
        sb.append(" (단백질 ").append(avgP).append("g / 탄수 ").append(avgC).append("g / 지방 ").append(avgF).append("g)\n");
        sb.append("- 기록이 없는 날: ").append(missing.isEmpty() ? "없음 (매일 기록 👍)" : String.join(", ", missing)).append("\n");
        sb.append("- ").append(macroHighlight(avgP, avgC, avgF));
        return sb.toString();
    }

    /** 평균 매크로에서 목표 대비 가장 부족/초과한 항목을 한 줄로 */
    private String macroHighlight(int p, int c, int f) {
        // 목표 대비 비율(<1 부족, >1 초과)
        double rp = p / (double) TARGET_PROTEIN, rc = c / (double) TARGET_CARBS, rf = f / (double) TARGET_FAT;
        String mostShort = rp <= rc && rp <= rf ? "단백질" : (rc <= rf ? "탄수화물" : "지방");
        String mostOver = rp >= rc && rp >= rf ? "단백질" : (rc >= rf ? "탄수화물" : "지방");
        return "가장 부족: " + mostShort + " · 가장 여유/초과: " + mostOver;
    }

    /** 끼니 목록 → 총합·끼니 상태 요약 문자열 */
    private String summarize(String label, List<Diet> diets) {
        if (diets == null || diets.isEmpty()) {
            return "[" + label + " 영양 요약]\n- 기록된 식단이 없습니다.";
        }
        int kcal = diets.stream().mapToInt(d -> nz(d.getTotalKcal())).sum();
        int protein = diets.stream().mapToInt(d -> nz(d.getProtein())).sum();
        int carbs = diets.stream().mapToInt(d -> nz(d.getCarbs())).sum();
        int fat = diets.stream().mapToInt(d -> nz(d.getFat())).sum();
        int remaining = targetKcal - kcal;

        Set<String> mealsLogged = new HashSet<>();
        diets.forEach(d -> mealsLogged.add(d.getMeal()));

        StringBuilder status = new StringBuilder();
        for (String[] m : MEALS) {
            if (status.length() > 0) status.append(", ");
            status.append(m[1]).append(mealsLogged.contains(m[0]) ? " 완료" : " 미기록");
        }

        StringBuilder sb = new StringBuilder("[" + label + " 영양 요약]\n");
        sb.append("- 총 섭취: ").append(kcal).append(" / ").append(targetKcal).append("kcal\n");
        // '일반 목표(2000kcal) 대비' 남은 값임을 라벨로 명확히 — 챌린지 한도까지 남은 값과 다름!
        sb.append(remaining >= 0
                ? "- 목표(" + targetKcal + "kcal) 대비 남은: " + remaining + "kcal\n"
                : "- 목표(" + targetKcal + "kcal) 초과: " + (-remaining) + "kcal\n");
        sb.append("- 단백질: ").append(protein).append("g / ").append(TARGET_PROTEIN).append("g\n");
        sb.append("- 탄수화물: ").append(carbs).append("g / ").append(TARGET_CARBS).append("g\n");
        sb.append("- 지방: ").append(fat).append("g / ").append(TARGET_FAT).append("g\n");
        sb.append("- 기록 상태: ").append(status).append(" (").append(diets.size()).append("끼)");
        return sb.toString();
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
