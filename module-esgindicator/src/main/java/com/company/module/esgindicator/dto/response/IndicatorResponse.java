package com.company.module.esgindicator.dto.response;

import com.company.module.esgindicator.entity.EsiIndicator;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IndicatorResponse {

    private Long indicatorId;
    private String esgCategory;
    private String esgCategoryName;
    private Long deptId;
    private String deptName;
    private String title;
    private String majorCategory;
    private String midCategory;
    private String minorCategory;
    private String unit;
    private Integer excelRowNum;
    private Integer sortOrder;
    private boolean useYn;
    private String remark;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IndicatorResponse from(EsiIndicator indicator) {
        return IndicatorResponse.builder()
                .indicatorId(indicator.getIndicatorId())
                .esgCategory(indicator.getEsgCategory().name())
                .esgCategoryName(indicator.getEsgCategory().getKorName())
                .deptId(indicator.getDepartment() != null ? indicator.getDepartment().getDeptId() : null)
                .deptName(indicator.getDepartment() != null ? indicator.getDepartment().getDeptName() : null)
                .title(indicator.getTitle())
                .majorCategory(indicator.getMajorCategory())
                .midCategory(indicator.getMidCategory())
                .minorCategory(indicator.getMinorCategory())
                .unit(indicator.getUnit())
                .excelRowNum(indicator.getExcelRowNum())
                .sortOrder(indicator.getSortOrder())
                .useYn(indicator.isUseYn())
                .remark(indicator.getRemark())
                .fullName(indicator.getFullName())
                .createdAt(indicator.getCreatedAt())
                .updatedAt(indicator.getUpdatedAt())
                .build();
    }
}
