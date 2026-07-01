package com.ssafy.nyamnyam.common;

/**
 * 사용자 입력 정제·검증 유틸 (서버측 방어).
 *
 * - 저장형 XSS 방어: 스크립트/위험 태그·이벤트 핸들러·javascript: 스킴을 제거한다.
 *   (프론트는 기본적으로 {{ }} 보간으로 escape 되지만, 서버에서도 한 번 더 막는다.)
 * - 일반 텍스트(예: "탄수 < 단백질")는 손상시키지 않도록, 모든 '<'를 지우지 않고
 *   위험 패턴만 선별 제거한다.
 * - 공백/길이 검증을 함께 제공한다.
 */
public final class InputSanitizer {

    private InputSanitizer() {}

    /** 위험 패턴 제거 + 앞뒤 공백 정리. null 은 null 그대로. */
    public static String clean(String s) {
        if (s == null) return null;
        String t = s.strip();
        t = t.replaceAll("(?is)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>", "");
        t = t.replaceAll("(?is)<\\s*/?\\s*(script|iframe|object|embed|style|link|meta)[^>]*>", "");
        t = t.replaceAll("(?i)\\son\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", "");
        t = t.replaceAll("(?i)javascript:", "");
        return t.strip();
    }

    /** 필수 입력: 정제 후 비어 있으면 400, 길이 초과면 400. 정제값 반환. */
    public static String required(String s, String field, int max) {
        String t = clean(s);
        if (t == null || t.isBlank()) {
            throw CustomException.badRequest(field + "을(를) 입력해 주세요.");
        }
        if (t.length() > max) {
            throw CustomException.badRequest(field + "은(는) 최대 " + max + "자까지 입력할 수 있어요.");
        }
        return t;
    }

    /** 선택 입력: 비어 있으면 null, 길이 초과면 400. 정제값 반환. */
    public static String optional(String s, String field, int max) {
        String t = clean(s);
        if (t == null || t.isBlank()) return null;
        if (t.length() > max) {
            throw CustomException.badRequest(field + "은(는) 최대 " + max + "자까지 입력할 수 있어요.");
        }
        return t;
    }

    /** 정수 범위 검증(필드가 null 이면 통과). 범위를 벗어나면 400. */
    public static Integer range(Integer v, String field, int min, int max) {
        if (v == null) return null;
        if (v < min || v > max) {
            throw CustomException.badRequest(field + "은(는) " + min + "~" + max + " 범위로 입력해 주세요.");
        }
        return v;
    }
}
