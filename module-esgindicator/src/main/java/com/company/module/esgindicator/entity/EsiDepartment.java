package com.company.module.esgindicator.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 부서 마스터 (ESG 지표 담당부서)
 * 테이블: MOD_ESGINDICATOR_DEPT
 */
@Entity
@Table(name = "MOD_ESGINDICATOR_DEPT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EsiDepartment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    /** 부서 코드 */
    @Column(name = "dept_code", length = 30, nullable = false, unique = true)
    private String deptCode;

    /** 부서명 */
    @Column(name = "dept_name", length = 100, nullable = false)
    private String deptName;

    /** 사용여부 */
    @Column(name = "use_yn", nullable = false)
    private boolean useYn;

    /** 정렬순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────
    public void update(String deptName, boolean useYn, Integer sortOrder) {
        this.deptName  = deptName;
        this.useYn     = useYn;
        this.sortOrder = sortOrder;
    }
}
