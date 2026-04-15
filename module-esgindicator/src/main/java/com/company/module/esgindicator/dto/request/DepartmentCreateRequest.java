package com.company.module.esgindicator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DepartmentCreateRequest {

    @NotBlank(message = "부서 코드는 필수입니다.")
    @Size(max = 30, message = "부서 코드는 30자 이내여야 합니다.")
    private String deptCode;

    @NotBlank(message = "부서명은 필수입니다.")
    @Size(max = 100, message = "부서명은 100자 이내여야 합니다.")
    private String deptName;

    @NotNull(message = "정렬순서는 필수입니다.")
    private Integer sortOrder;

    private boolean useYn = true;
}
