package com.ssafy.nyamnyam.domain.member;

import org.springframework.stereotype.Component;

/**
 * 개인 목표 칼로리 계산기 (결정론적, LLM 비의존).
 *
 * 순서: BMR(Mifflin-St Jeor) → 활동량 설문 점수 → 활동계수 → TDEE → 목표유형 계수 → 10kcal 반올림 → 성별 안전범위.
 *
 * 반올림 정책(명세 기준):
 *  - BMR: 반올림(Math.round)
 *  - TDEE: 버림(Math.floor)  ← 명세 예시 2687.7 → 2687
 *  - 목표 적용값: 반올림
 *  - 최종: 10kcal 단위 반올림 후 성별 안전범위 클램프
 */
@Component
public class CalorieTargetCalculator {

    /** 계산 결과 */
    public record Result(int targetCalories, int bmr, int tdee, String activityLevel, int activityScore) {}

    public Result calculate(Gender gender, int age, int heightCm, int weightKg, GoalType goal,
                            int jobActivity, int exerciseFrequency, int exerciseIntensity,
                            int dailySteps, int weeklyExerciseHours) {

        int activityScore = jobActivity + exerciseFrequency + exerciseIntensity + dailySteps + weeklyExerciseHours;
        ActivityLevel level = ActivityLevel.fromScore(activityScore);

        double bmrRaw = (gender == Gender.MALE)
                ? 10.0 * weightKg + 6.25 * heightCm - 5.0 * age + 5
                : 10.0 * weightKg + 6.25 * heightCm - 5.0 * age - 161;
        int bmr = (int) Math.round(bmrRaw);

        int tdee = (int) Math.floor(bmr * level.multiplier());          // 버림
        int goalApplied = (int) Math.round(tdee * goal.factor());       // 목표 적용 후 반올림
        int target = (int) (Math.round(goalApplied / 10.0) * 10);       // 10kcal 단위 반올림

        // 성별 안전범위 (10kcal 반올림 이후 적용)
        int min = (gender == Gender.MALE) ? 1500 : 1200;
        int max = (gender == Gender.MALE) ? 4500 : 4000;
        target = Math.max(min, Math.min(max, target));

        return new Result(target, bmr, tdee, level.name(), activityScore);
    }
}
