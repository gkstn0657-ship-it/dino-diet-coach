package com.ssafy.nyamnyam.common;

import java.util.Set;

/** 운영 권한 역할 정의 (OPERATOR 신규 + ADMIN 기존 호환). 도메인 곳곳의 중복 정의를 단일화. */
public final class Roles {
    private Roles() {}

    public static final Set<String> OPERATOR_ROLES = Set.of("OPERATOR", "ADMIN");

    /** role 이 운영 권한(OPERATOR/ADMIN)인지 (null 안전) */
    public static boolean isOperator(String role) {
        return role != null && OPERATOR_ROLES.contains(role);
    }
}
