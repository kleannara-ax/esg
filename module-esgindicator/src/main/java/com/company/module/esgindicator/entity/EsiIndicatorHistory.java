package com.company.module.esgindicator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ESG 지표값 입력/수정 이력
 * 테이블: MOD_ESGINDICATOR_HISTORY
 *
 * EsiIndicatorValue 의 변경이 발생할 때마다 이력을 쌓는다.
 * (BaseEntity 상속 없이 최소 필드만 유지)
 */
@Entity
@Table(
    name = "MOD_ESGINDICATOR_HISTORY",
    indexes = {
        @Index(name = "IDX_ESI_HIST_VALUE", columnList = "value_id"),
        @Index(name = "IDX_ESI_HIST_DATE",  columnList = "changed_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EsiIndicatorHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    /** 원본 지표값 레코드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "value_id", nullable = false)
    private EsiIndicatorValue indicatorValue;

    /** 변경 유형 (CREATE / UPDATE / SUBMIT / CONFIRM / CANCEL) */
    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType;

    /** 변경 전 값 */
    @Column(name = "before_value", precision = 20, scale = 6)
    private BigDecimal beforeValue;

    /** 변경 후 값 */
    @Column(name = "after_value", precision = 20, scale = 6)
    private BigDecimal afterValue;

    /** 변경 전 상태 */
    @Column(name = "before_status", length = 20)
    private String beforeStatus;

    /** 변경 후 상태 */
    @Column(name = "after_status", length = 20)
    private String afterStatus;

    /** 변경 비고 */
    @Column(name = "change_remark", length = 500)
    private String changeRemark;

    /** 변경자 ID */
    @Column(name = "changed_by", length = 50, nullable = false)
    private String changedBy;

    /** 변경일시 */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // ── 팩토리 메서드 ────────────────────────────────────────────────────────

    public static EsiIndicatorHistory of(
            EsiIndicatorValue value,
            String changeType,
            BigDecimal beforeValue,
            BigDecimal afterValue,
            String beforeStatus,
            String afterStatus,
            String changeRemark,
            String changedBy) {

        return EsiIndicatorHistory.builder()
                .indicatorValue(value)
                .changeType(changeType)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .changeRemark(changeRemark)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
