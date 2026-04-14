package com.company.module.esg.dto.request;

import com.company.module.esg.entity.EsgCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ESG 지표 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class EsgIndicatorCreateRequest {

    @NotNull(message = "ESG 카테고리는 필수입니다. (E/S/G)")
    private EsgCategory category;

    @NotBlank(message = "지표 코드는 필수입니다.")
    @Size(max = 50, message = "지표 코드는 50자 이하여야 합니다.")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "지표 코드는 대문자, 숫자, _ 만 사용 가능합니다.")
    private String indicatorCode;

    @NotBlank(message = "지표명은 필수입니다.")
    @Size(max = 200, message = "지표명은 200자 이하여야 합니다.")
    private String indicatorName;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    private String description;

    @Size(max = 50, message = "단위는 50자 이하여야 합니다.")
    private String unit;
}
