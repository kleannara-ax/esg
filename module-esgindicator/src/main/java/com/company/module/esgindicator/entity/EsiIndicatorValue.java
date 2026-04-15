package com.company.module.esgindicator.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 연도별 ESG 지표값
 * 테이블: MOD_ESGINDICATOR_VALUE
 *
 * 지표 마스터(EsiIndicator) × 부서(EsiDepartment) × 연도 의 복합 데이터
 */
@Entity
@Table(
    name = "MOD_ESGINDICATOR_VALUE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_ESI_VALUE",
            columnNames = {"indicator_id", "dept_id", "base_year"}
        )
    },
    indexes = {
        @Index(name = "IDX_ESI_VALUE_YEAR",  columnList = "base_year"),
        @Index(name = "IDX_ESI_VALUE_DEPT",  columnList = "dept_id"),
        @Index(name = "IDX_ESI_VALUE_INDIC", columnList = "indicator_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EsiIndicatorValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Long valueId;

    /** 지표 마스터 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "indicator_id", nullable = false)
    private EsiIndicator indicator;

    /** 담당부서 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dept_id", nullable = false)
    private EsiDepartment department;

    /** 기준연도 (예: 2024) */
    @Column(name = "base_year", nullable = false)
    private Integer baseYear;

    /**
     * 지표값
     * precision=20, scale=6 → 최대 14자리 정수 + 소수점 6자리 지원
     * NULL = 미입력
     */
    @Column(name = "indicator_value", precision = 20, scale = 6)
    private BigDecimal indicatorValue;

    /** 산식/비고 (계산 근거 또는 메모) */
    @Column(name = "formula_remark", columnDefinition = "TEXT")
    private String formulaRemark;

    /** 입력 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "input_status", length = 20, nullable = false)
    @Builder.Default
    private InputStatus inputStatus = InputStatus.NOT_STARTED;

    /** 최종확정여부 */
    @Column(name = "confirmed_yn", nullable = false)
    @Builder.Default
    private boolean confirmedYn = false;

    /** 확정 비고 */
    @Column(name = "confirm_remark", length = 500)
    private String confirmRemark;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────

    /** 값 입력/수정 */
    public void updateValue(BigDecimal value, String formulaRemark) {
        this.indicatorValue = value;
        this.formulaRemark  = formulaRemark;
        this.inputStatus    = (value != null) ? InputStatus.IN_PROGRESS : InputStatus.NOT_STARTED;
    }

    /** 제출 */
    public void submit() {
        if (this.indicatorValue == null) {
            throw new IllegalStateException("값을 먼저 입력해야 제출할 수 있습니다.");
        }
        this.inputStatus = InputStatus.SUBMITTED;
    }

    /** 최종 확정 */
    public void confirm(String remark) {
        if (this.inputStatus != InputStatus.SUBMITTED) {
            throw new IllegalStateException("제출 완료 상태에서만 확정할 수 있습니다.");
        }
        this.confirmedYn   = true;
        this.inputStatus   = InputStatus.CONFIRMED;
        this.confirmRemark = remark;
    }

    /** 확정 취소 */
    public void cancelConfirm() {
        this.confirmedYn   = false;
        this.inputStatus   = InputStatus.SUBMITTED;
        this.confirmRemark = null;
    }

    /** 부서 변경 */
    public void changeDepartment(EsiDepartment department) {
        this.department = department;
    }
}
