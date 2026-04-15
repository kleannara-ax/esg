package com.company.module.esgindicator.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 입력 현황 요약 응답 DTO
 * 화면 상단 통계 표시용
 */
@Getter
@Builder
public class InputStatusSummaryResponse {

    private Integer baseYear;
    private String esgCategory;
    private String esgCategoryName;

    /** 전체 지표 수 (지표마스터 × 부서) */
    private long totalCount;

    /** 미입력 건수 */
    private long notStartedCount;

    /** 입력중 건수 */
    private long inProgressCount;

    /** 제출완료 건수 */
    private long submittedCount;

    /** 최종확정 건수 */
    private long confirmedCount;

    /** 입력완료율 (%) - (제출+확정) / 전체 */
    public double getCompletionRate() {
        if (totalCount == 0) return 0.0;
        return Math.round((double)(submittedCount + confirmedCount) / totalCount * 10000.0) / 100.0;
    }

    /** 미입력률 (%) */
    public double getNotStartedRate() {
        if (totalCount == 0) return 0.0;
        return Math.round((double) notStartedCount / totalCount * 10000.0) / 100.0;
    }
}
