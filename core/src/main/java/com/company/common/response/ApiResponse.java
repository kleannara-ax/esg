package com.company.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 래퍼
 * <pre>
 * {
 *   "success": true,
 *   "code": "SUCCESS",
 *   "message": "처리가 완료되었습니다.",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T00:00:00"
 * }
 * </pre>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ── 성공 응답 ────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return of(true, "SUCCESS", "처리가 완료되었습니다.", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(true, "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(true, "CREATED", "등록이 완료되었습니다.", data);
    }

    // ── 실패 응답 ────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> fail(String code, String message) {
        return of(false, code, message, null);
    }

    // ── 내부 팩토리 ──────────────────────────────────────────────────────────

    private static <T> ApiResponse<T> of(boolean success, String code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success   = success;
        response.code      = code;
        response.message   = message;
        response.data      = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}
