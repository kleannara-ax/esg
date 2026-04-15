package com.company.module.esgindicator.dto.response;

import com.company.module.esgindicator.entity.EsiIndicatorValue;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class IndicatorValueResponse {

    private Long valueId;
    private Long indicatorId;
    private String minorCategory;   // 지표명
    private String unit;
    private String majorCategory;
    private String midCategory;
    private String esgCategory;
    private Long deptId;
    private String deptName;
    private Integer baseYear;
    private BigDecimal indicatorValue;
    private String formulaRemark;
    private String inputStatus;
    private String inputStatusLabel;
    private boolean confirmedYn;
    private String confirmRemark;
    private Integer sortOrder;
    private LocalDateTime updatedAt;

    public static IndicatorValueResponse from(EsiIndicatorValue v) {
        return IndicatorValueResponse.builder()
                .valueId(v.getValueId())
                .indicatorId(v.getIndicator().getIndicatorId())
                .minorCategory(v.getIndicator().getMinorCategory())
                .unit(v.getIndicator().getUnit())
                .majorCategory(v.getIndicator().getMajorCategory())
                .midCategory(v.getIndicator().getMidCategory())
                .esgCategory(v.getIndicator().getEsgCategory().name())
                .deptId(v.getDepartment().getDeptId())
                .deptName(v.getDepartment().getDeptName())
                .baseYear(v.getBaseYear())
                .indicatorValue(v.getIndicatorValue())
                .formulaRemark(v.getFormulaRemark())
                .inputStatus(v.getInputStatus().name())
                .inputStatusLabel(v.getInputStatus().getLabel())
                .confirmedYn(v.isConfirmedYn())
                .confirmRemark(v.getConfirmRemark())
                .sortOrder(v.getIndicator().getSortOrder())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
