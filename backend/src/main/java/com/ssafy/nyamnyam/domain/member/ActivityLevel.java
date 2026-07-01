package com.ssafy.nyamnyam.domain.member;

/**
 * 활동 수준 — 활동량 설문 점수 합(0~20)으로 결정되며 TDEE 계수를 보유한다.
 * 점수 구간: SEDENTARY 0~4 / LIGHT 5~8 / MODERATE 9~12 / ACTIVE 13~16 / VERY_ACTIVE 17~20
 */
public enum ActivityLevel {
    SEDENTARY(1.20, 0, 4),
    LIGHT(1.375, 5, 8),
    MODERATE(1.55, 9, 12),
    ACTIVE(1.725, 13, 16),
    VERY_ACTIVE(1.90, 17, 20);

    private final double multiplier;
    private final int minScore;
    private final int maxScore;

    ActivityLevel(double multiplier, int minScore, int maxScore) {
        this.multiplier = multiplier;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public double multiplier() {
        return multiplier;
    }

    /** 활동량 점수 합 → 활동 수준 */
    public static ActivityLevel fromScore(int score) {
        for (ActivityLevel a : values()) {
            if (score >= a.minScore && score <= a.maxScore) return a;
        }
        return score < 0 ? SEDENTARY : VERY_ACTIVE; // 범위 밖 방어
    }
}
