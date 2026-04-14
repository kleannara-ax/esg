package com.company.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 시스템 공통 에러 코드
 */
@Getter
public enum ErrorCode {

    // ── 공통 ──────────────────────────────────────────────────────────────────
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE   (HttpStatus.BAD_REQUEST,          "C002", "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND    (HttpStatus.NOT_FOUND,            "C003", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE    (HttpStatus.CONFLICT,             "C004", "이미 존재하는 리소스입니다."),
    ACCESS_DENIED         (HttpStatus.FORBIDDEN,            "C005", "접근 권한이 없습니다."),

    // ── 인증 ──────────────────────────────────────────────────────────────────
    UNAUTHORIZED          (HttpStatus.UNAUTHORIZED,         "A001", "인증이 필요합니다."),
    INVALID_TOKEN         (HttpStatus.UNAUTHORIZED,         "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN         (HttpStatus.UNAUTHORIZED,         "A003", "만료된 토큰입니다."),
    INVALID_CREDENTIALS   (HttpStatus.UNAUTHORIZED,         "A004", "아이디 또는 비밀번호가 올바르지 않습니다."),

    // ── 사용자 ────────────────────────────────────────────────────────────────
    USER_NOT_FOUND        (HttpStatus.NOT_FOUND,            "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS   (HttpStatus.CONFLICT,             "U002", "이미 존재하는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code       = code;
        this.message    = message;
    }
}
