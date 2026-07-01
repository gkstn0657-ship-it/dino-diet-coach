package com.ssafy.nyamnyam.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustom(CustomException e) {
        // 비즈니스 예외는 의도된 메시지이므로 그대로 내려줌
        // payload 가 있으면 함께 내려줌 (예: 슬롯 중복 시 기존 dno)
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>("ERROR", e.getMessage(), e.getPayload()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("유효성 검증 실패");
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        // 내부 예외 상세(메시지·스택)는 서버 로그에만 남기고,
        // 클라이언트에는 내부 구현을 노출하지 않는 일반 메시지만 전달한다.
        log.error("처리되지 않은 서버 오류", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
