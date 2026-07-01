package com.ssafy.nyamnyam.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 응답 포맷: 프론트엔드가 res.data.payload 로 접근.
 * { status, message, payload }
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;   // SUCCESS | ERROR
    private String message;
    private T payload;

    public static <T> ApiResponse<T> ok(T payload) {
        return new ApiResponse<>("SUCCESS", "OK", payload);
    }

    public static <T> ApiResponse<T> ok(String message, T payload) {
        return new ApiResponse<>("SUCCESS", message, payload);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}
