package com.company.module.esg.dto.response;

import com.company.module.esg.entity.EntryStatus;
import com.company.module.esg.entity.EsgDataEntry;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ESG 데이터 입력 응답 DTO
 */
@Getter
@Builder
public class EsgDataEntryResponse {

    private Long entryId;
    private Long indicatorId;
    private String indicatorCode;
    private String indicatorName;
    private String categoryName;
    private Integer targetYear;
    private Integer targetMonth;
    private BigDecimal actualValue;
    private BigDecimal targetValue;
    private BigDecimal achievementRate;
    private String unit;
    private String remark;
    private EntryStatus entryStatus;
    private String entryStatusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EsgDataEntryResponse from(EsgDataEntry entry) {
        return EsgDataEntryResponse.builder()
                .entryId(entry.getEntryId())
                .indicatorId(entry.getIndicator().getIndicatorId())
                .indicatorCode(entry.getIndicator().getIndicatorCode())
                .indicatorName(entry.getIndicator().getIndicatorName())
                .categoryName(entry.getIndicator().getCategory().getKorName())
                .targetYear(entry.getTargetYear())
                .targetMonth(entry.getTargetMonth())
                .actualValue(entry.getActualValue())
                .targetValue(entry.getTargetValue())
                .achievementRate(entry.calculateAchievementRate())
                .unit(entry.getIndicator().getUnit())
                .remark(entry.getRemark())
                .entryStatus(entry.getEntryStatus())
                .entryStatusName(entry.getEntryStatus().name())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
