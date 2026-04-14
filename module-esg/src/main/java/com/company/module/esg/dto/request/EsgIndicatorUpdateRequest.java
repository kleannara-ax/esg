package com.company.module.esg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ESG 지표 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class EsgIndicatorUpdateRequest {

    @NotBlank(message = "지표명은 필수입니다.")
    @Size(max = 200, message = "지표명은 200자 이하여야 합니다.")
    private String indicatorName;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    private String description;

    @Size(max = 50, message = "단위는 50자 이하여야 합니다.")
    private String unit;
}
