package com.company.module.esgindicator.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 지표값 저장/수정 요청
 * 단건 또는 배치(bulk) 모두 사용
 */
@Getter
@NoArgsConstructor
public class ValueSaveRequest {

    @NotNull(message = "지표 ID는 필수입니다.")
    private Long indicatorId;

    @NotNull(message = "부서 ID는 필수입니다.")
    private Long deptId;

    @NotNull(message = "기준연도는 필수입니다.")
    private Integer baseYear;

    /** null 허용 - 미입력 상태로 저장 */
    private BigDecimal indicatorValue;

    private String formulaRemark;
}
