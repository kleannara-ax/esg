package com.company.module.esgindicator.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 연도별 지표값 일괄 저장 요청 (엑셀 화면의 저장 버튼)
 */
@Getter
@NoArgsConstructor
public class ValueBulkSaveRequest {

    @NotNull(message = "기준연도는 필수입니다.")
    private Integer baseYear;

    @NotNull(message = "부서 ID는 필수입니다.")
    private Long deptId;

    @Valid
    @NotEmpty(message = "저장할 지표값이 없습니다.")
    private List<ValueItemRequest> items;

    @Getter
    @NoArgsConstructor
    public static class ValueItemRequest {

        @NotNull(message = "지표 ID는 필수입니다.")
        private Long indicatorId;

        private java.math.BigDecimal indicatorValue;

        private String formulaRemark;
    }
}
