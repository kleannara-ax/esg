package com.company.module.esgindicator.dto.response;

import com.company.module.esgindicator.entity.EsiDepartment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DepartmentResponse {

    private Long deptId;
    private String deptCode;
    private String deptName;
    private boolean useYn;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse from(EsiDepartment dept) {
        return DepartmentResponse.builder()
                .deptId(dept.getDeptId())
                .deptCode(dept.getDeptCode())
                .deptName(dept.getDeptName())
                .useYn(dept.isUseYn())
                .sortOrder(dept.getSortOrder())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
