package com.company.module.esg.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ESG 지표 마스터 엔티티
 * <p>
 * 테이블명: MOD_ESG_INDICATOR
 * URL Prefix: /esg-api/indicators
 * </p>
 */
@Entity
@Table(name = "MOD_ESG_INDICATOR")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EsgIndicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    /** ESG 카테고리 (E/S/G) */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 1, nullable = false)
    private EsgCategory category;

    /** 지표 코드 */
    @Column(name = "indicator_code", length = 50, nullable = false, unique = true)
    private String indicatorCode;

    /** 지표명 */
    @Column(name = "indicator_name", length = 200, nullable = false)
    private String indicatorName;

    /** 지표 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 측정 단위 (예: tCO2eq, kWh, %, 명) */
    @Column(name = "unit", length = 50)
    private String unit;

    /** 활성화 여부 */
    @Column(name = "active_yn", nullable = false)
    private boolean activeYn;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────

    public void update(String indicatorName, String description, String unit) {
        this.indicatorName = indicatorName;
        this.description   = description;
        this.unit          = unit;
    }

    public void deactivate() {
        this.activeYn = false;
    }

    public void activate() {
        this.activeYn = true;
    }
}
