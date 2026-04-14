package com.company.module.esg.exception;

import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

/**
 * ESG 모듈 전용 예외
 * <p>
 * core 의 BusinessException 을 상속하여
 * GlobalExceptionHandler 에서 자동으로 처리된다.
 * </p>
 */
public class EsgException extends BusinessException {

    public EsgException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EsgException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ── ESG 도메인 특화 팩토리 메서드 ────────────────────────────────────────

    public static EsgException indicatorNotFound(Long indicatorId) {
        return new EsgException(ErrorCode.RESOURCE_NOT_FOUND,
                "ESG 지표를 찾을 수 없습니다. ID=" + indicatorId);
    }

    public static EsgException indicatorCodeDuplicated(String code) {
        return new EsgException(ErrorCode.DUPLICATE_RESOURCE,
                "이미 존재하는 지표 코드입니다: " + code);
    }

    public static EsgException dataEntryNotFound(Long entryId) {
        return new EsgException(ErrorCode.RESOURCE_NOT_FOUND,
                "ESG 데이터 입력 정보를 찾을 수 없습니다. ID=" + entryId);
    }

    public static EsgException dataEntryDuplicated(Long indicatorId, int year, int month) {
        return new EsgException(ErrorCode.DUPLICATE_RESOURCE,
                String.format("이미 입력된 데이터가 존재합니다. indicatorId=%d, %d년 %d월",
                        indicatorId, year, month));
    }

    public static EsgException invalidStatusTransition(String from, String to) {
        return new EsgException(ErrorCode.INVALID_INPUT_VALUE,
                String.format("상태 전환이 불가합니다: %s → %s", from, to));
    }
}
