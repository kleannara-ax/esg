package com.company.module.esgindicator.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 엑셀 시트 형태의 화면 렌더링 응답
 * 행: 지표, 열: 부서
 */
@Getter
@Builder
public class IndicatorSheetResponse {

    private Integer baseYear;
    private String esgCategory;
    private String esgCategoryName;

    /** 열 헤더(부서) 목록 */
    private List<DepartmentResponse> departments;

    /** 행(지표) 목록 - 각 행에 부서별 값 포함 */
    private List<IndicatorRowResponse> rows;

    /** 입력 현황 요약 */
    private InputStatusSummaryResponse summary;

    @Getter
    @Builder
    public static class IndicatorRowResponse {
        private Long indicatorId;
        private String majorCategory;
        private String midCategory;
        private String minorCategory;
        private String unit;
        private Integer sortOrder;

        /**
         * 부서별 값 목록 (departments 리스트와 같은 순서)
         * null → 해당 연도 미생성 (데이터 없음)
         */
        private List<IndicatorValueResponse> values;
    }
}
