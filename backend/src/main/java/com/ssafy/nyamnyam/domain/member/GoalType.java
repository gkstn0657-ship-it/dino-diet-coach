package com.ssafy.nyamnyam.domain.member;

/** 체중 관리 목표 유형 — TDEE 에 곱하는 계수 보유 */
public enum GoalType {
    DIET(0.85, "감량"),
    MAINTAIN(1.00, "유지"),
    BULK_UP(1.15, "증량");

    private final double factor;
    private final String label;

    GoalType(double factor, String label) {
        this.factor = factor;
        this.label = label;
    }

    public double factor() {
        return factor;
    }

    /** 마이페이지 등 표시용 한글 라벨 */
    public String label() {
        return label;
    }

    /** "DIET" 등 이름으로 매칭. 실패 시 null */
    public static GoalType from(String s) {
        if (s == null) return null;
        for (GoalType g : values()) {
            if (g.name().equalsIgnoreCase(s)) return g;
        }
        return null;
    }
}
