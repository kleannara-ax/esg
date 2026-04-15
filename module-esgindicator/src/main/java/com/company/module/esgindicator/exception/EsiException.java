package com.company.module.esgindicator.exception;

import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;

/**
 * module-esgindicator 업무 예외
 * Core 의 BusinessException 을 상속하여 동일한 GlobalExceptionHandler 에서 처리됨
 */
public class EsiException extends BusinessException {

    public EsiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EsiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ── 편의 팩토리 메서드 ───────────────────────────────────────────────────

    public static EsiException indicatorNotFound(Long indicatorId) {
        return new EsiException(ErrorCode.RESOURCE_NOT_FOUND,
                "지표를 찾을 수 없습니다. id=" + indicatorId);
    }

    public static EsiException departmentNotFound(Long deptId) {
        return new EsiException(ErrorCode.RESOURCE_NOT_FOUND,
                "부서를 찾을 수 없습니다. id=" + deptId);
    }

    public static EsiException departmentNotFound(String deptCode) {
        return new EsiException(ErrorCode.RESOURCE_NOT_FOUND,
                "부서를 찾을 수 없습니다. code=" + deptCode);
    }

    public static EsiException valueNotFound(Long valueId) {
        return new EsiException(ErrorCode.RESOURCE_NOT_FOUND,
                "지표값을 찾을 수 없습니다. id=" + valueId);
    }

    public static EsiException duplicateDeptCode(String deptCode) {
        return new EsiException(ErrorCode.DUPLICATE_RESOURCE,
                "이미 사용 중인 부서 코드입니다. code=" + deptCode);
    }

    public static EsiException alreadyConfirmed(Long valueId) {
        return new EsiException(ErrorCode.INVALID_INPUT_VALUE,
                "이미 최종 확정된 지표값입니다. id=" + valueId);
    }

    public static EsiException invalidStatus(String message) {
        return new EsiException(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}
