package com.company.module.esg.entity;

/**
 * ESG 데이터 입력 상태
 */
public enum EntryStatus {
    DRAFT,      // 작성 중
    SUBMITTED,  // 제출 완료
    APPROVED,   // 승인 완료
    REJECTED    // 반려
}
