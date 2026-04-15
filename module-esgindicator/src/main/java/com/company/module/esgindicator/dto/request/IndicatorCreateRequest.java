package com.company.module.esgindicator.dto.request;

import com.company.module.esgindicator.entity.EsgCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndicatorCreateRequest {

    @NotNull(message = "ESG 구분은 필수입니다.")
    private EsgCategory esgCategory;

    /** 부서 ID (nullable - 부서 미지정 가능) */
    private Long deptId;

    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String title;

    @Size(max = 200, message = "표 대제목은 200자 이내여야 합니다.")
    private String majorCategory;

    @Size(max = 200, message = "표 중제목은 200자 이내여야 합니다.")
    private String midCategory;

    @NotBlank(message = "지표명(표 소제목)은 필수입니다.")
    @Size(max = 300, message = "지표명은 300자 이내여야 합니다.")
    private String minorCategory;

    @Size(max = 50, message = "단위는 50자 이내여야 합니다.")
    private String unit;

    private Integer excelRowNum;

    @NotNull(message = "정렬순서는 필수입니다.")
    private Integer sortOrder;

    private boolean useYn = true;

    private String remark;
}
