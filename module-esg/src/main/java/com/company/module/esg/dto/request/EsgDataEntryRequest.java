package com.company.module.esg.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ESG 데이터 입력 요청 DTO
 */
@Getter
@NoArgsConstructor
public class EsgDataEntryRequest {

    @NotNull(message = "지표 ID는 필수입니다.")
    private Long indicatorId;

    @NotNull(message = "대상 연도는 필수입니다.")
    @Min(value = 2000, message = "대상 연도는 2000 이상이어야 합니다.")
    @Max(value = 2099, message = "대상 연도는 2099 이하여야 합니다.")
    private Integer targetYear;

    @NotNull(message = "대상 월은 필수입니다.")
    @Min(value = 1,  message = "대상 월은 1~12 사이여야 합니다.")
    @Max(value = 12, message = "대상 월은 1~12 사이여야 합니다.")
    private Integer targetMonth;

    @DecimalMin(value = "0.0", message = "실적값은 0 이상이어야 합니다.")
    private BigDecimal actualValue;

    @DecimalMin(value = "0.0", message = "목표값은 0 이상이어야 합니다.")
    private BigDecimal targetValue;

    @Size(max = 2000, message = "비고는 2000자 이하여야 합니다.")
    private String remark;
}
