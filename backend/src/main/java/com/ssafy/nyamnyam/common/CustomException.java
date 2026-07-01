package com.ssafy.nyamnyam.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    /** 선택적 부가 데이터 (예: 슬롯 중복 시 기존 dno) — 응답 payload 로 전달 */
    private final Object payload;

    public CustomException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public CustomException(HttpStatus status, String message, Object payload) {
        super(message);
        this.status = status;
        this.payload = payload;
    }

    public static CustomException badRequest(String message) {
        return new CustomException(HttpStatus.BAD_REQUEST, message);
    }

    public static CustomException unauthorized(String message) {
        return new CustomException(HttpStatus.UNAUTHORIZED, message);
    }

    public static CustomException notFound(String message) {
        return new CustomException(HttpStatus.NOT_FOUND, message);
    }

    /** 권한 없음 (403) — 운영 콘솔 등 권한 필요한 API 차단용 */
    public static CustomException forbidden(String message) {
        return new CustomException(HttpStatus.FORBIDDEN, message);
    }

    /** 중복/충돌 (409). payload 로 기존 리소스 식별자 등을 함께 전달 */
    public static CustomException conflict(String message, Object payload) {
        return new CustomException(HttpStatus.CONFLICT, message, payload);
    }
}
