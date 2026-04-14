package com.company.module.esg.dto.response;

import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgIndicator;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ESG 지표 응답 DTO
 */
@Getter
@Builder
public class EsgIndicatorResponse {

    private Long indicatorId;
    private EsgCategory category;
    private String categoryName;
    private String indicatorCode;
    private String indicatorName;
    private String description;
    private String unit;
    private boolean activeYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EsgIndicatorResponse from(EsgIndicator indicator) {
        return EsgIndicatorResponse.builder()
                .indicatorId(indicator.getIndicatorId())
                .category(indicator.getCategory())
                .categoryName(indicator.getCategory().getKorName())
                .indicatorCode(indicator.getIndicatorCode())
                .indicatorName(indicator.getIndicatorName())
                .description(indicator.getDescription())
                .unit(indicator.getUnit())
                .activeYn(indicator.isActiveYn())
                .createdAt(indicator.getCreatedAt())
                .updatedAt(indicator.getUpdatedAt())
                .build();
    }
}
