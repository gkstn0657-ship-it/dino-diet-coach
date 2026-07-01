package com.ssafy.nyamnyam.domain.member;

/** 성별 — DB 에는 code("male"/"female") 로 저장 */
public enum Gender {
    MALE("male"),
    FEMALE("female");

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** "male"/"MALE" 등 코드/이름 모두 허용. 매칭 실패 시 null */
    public static Gender from(String s) {
        if (s == null) return null;
        for (Gender g : values()) {
            if (g.code.equalsIgnoreCase(s) || g.name().equalsIgnoreCase(s)) return g;
        }
        return null;
    }
}
