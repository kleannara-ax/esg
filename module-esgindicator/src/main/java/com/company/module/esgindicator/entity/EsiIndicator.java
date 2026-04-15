package com.company.module.esgindicator.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ESG 지표 마스터
 * 테이블: MOD_ESGINDICATOR_MASTER
 *
 * 엑셀 컬럼 매핑:
 *  담당부서(dept) / 제목(title) / 표대제목(majorCategory) /
 *  표중제목(midCategory) / 표소제목(minorCategory) / 단위(unit)
 */
@Entity
@Table(
    name = "MOD_ESGINDICATOR_MASTER",
    indexes = {
        @Index(name = "IDX_ESI_MASTER_CATEGORY",  columnList = "esg_category"),
        @Index(name = "IDX_ESI_MASTER_DEPT",       columnList = "dept_id"),
        @Index(name = "IDX_ESI_MASTER_SORT",       columnList = "sort_order")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EsiIndicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    /** ESG 구분 (E/S/G) */
    @Enumerated(EnumType.STRING)
    @Column(name = "esg_category", length = 1, nullable = false)
    private EsgCategory esgCategory;

    /** 담당부서 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private EsiDepartment department;

    /** 제목 (엑셀 '제목' 컬럼) - 예: 기후위기 대응 */
    @Column(name = "title", length = 200)
    private String title;

    /** 표 대제목 (예: 온실가스 배출) */
    @Column(name = "major_category", length = 200)
    private String majorCategory;

    /** 표 중제목 (예: 온실가스) */
    @Column(name = "mid_category", length = 200)
    private String midCategory;

    /** 표 소제목 = 실제 지표명 (예: 총 배출량(Scope 1 & 2)) */
    @Column(name = "minor_category", length = 300, nullable = false)
    private String minorCategory;

    /** 단위 (예: tCO2eq, %, 명, TJ) */
    @Column(name = "unit", length = 50)
    private String unit;

    /** 엑셀 행번호 (업로드 시 매핑용) */
    @Column(name = "excel_row_num")
    private Integer excelRowNum;

    /** 표시순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 사용여부 */
    @Column(name = "use_yn", nullable = false)
    private boolean useYn;

    /** 비고/설명 */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────
    public void update(String title, String majorCategory, String midCategory,
                       String minorCategory, String unit, String remark,
                       Integer sortOrder, boolean useYn) {
        this.title         = title;
        this.majorCategory = majorCategory;
        this.midCategory   = midCategory;
        this.minorCategory = minorCategory;
        this.unit          = unit;
        this.remark        = remark;
        this.sortOrder     = sortOrder;
        this.useYn         = useYn;
    }

    public void changeDepartment(EsiDepartment department) {
        this.department = department;
    }

    /** 표시용 전체 지표명 */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (majorCategory != null && !majorCategory.isBlank()) sb.append(majorCategory);
        if (midCategory   != null && !midCategory.isBlank())   sb.append(" > ").append(midCategory);
        if (minorCategory != null && !minorCategory.isBlank()) sb.append(" > ").append(minorCategory);
        return sb.toString();
    }
}
