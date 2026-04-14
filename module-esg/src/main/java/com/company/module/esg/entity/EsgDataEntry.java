package com.company.module.esg.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * ESG 데이터 입력 엔티티
 * <p>
 * 테이블명: MOD_ESG_DATA_ENTRY
 * 특정 연월의 ESG 지표 실적 데이터를 저장한다.
 * </p>
 */
@Entity
@Table(
    name = "MOD_ESG_DATA_ENTRY",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_ESG_DATA_ENTRY",
            columnNames = {"indicator_id", "target_year", "target_month"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EsgDataEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    /** 연결된 ESG 지표 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private EsgIndicator indicator;

    /** 대상 연도 */
    @Column(name = "target_year", nullable = false)
    private Integer targetYear;

    /** 대상 월 */
    @Column(name = "target_month", nullable = false)
    private Integer targetMonth;

    /** 실적값 */
    @Column(name = "actual_value", precision = 20, scale = 4)
    private BigDecimal actualValue;

    /** 목표값 */
    @Column(name = "target_value", precision = 20, scale = 4)
    private BigDecimal targetValue;

    /** 비고 */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /** 데이터 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_status", length = 20, nullable = false)
    private EntryStatus entryStatus;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────

    public void updateValues(BigDecimal actualValue, BigDecimal targetValue, String remark) {
        this.actualValue = actualValue;
        this.targetValue = targetValue;
        this.remark      = remark;
    }

    public void submit() {
        this.entryStatus = EntryStatus.SUBMITTED;
    }

    public void approve() {
        this.entryStatus = EntryStatus.APPROVED;
    }

    public void reject(String remark) {
        this.entryStatus = EntryStatus.REJECTED;
        this.remark      = remark;
    }

    /**
     * 달성률 계산 (%)
     */
    public BigDecimal calculateAchievementRate() {
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actualValue.multiply(BigDecimal.valueOf(100))
                .divide(targetValue, 2, java.math.RoundingMode.HALF_UP);
    }
}
