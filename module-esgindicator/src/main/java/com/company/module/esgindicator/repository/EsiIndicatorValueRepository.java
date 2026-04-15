package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.entity.EsiIndicatorValue;
import com.company.module.esgindicator.entity.InputStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 연도별 ESG 지표값 Repository
 */
public interface EsiIndicatorValueRepository extends JpaRepository<EsiIndicatorValue, Long> {

    /**
     * 연도 + ESG구분별 전체 지표값 조회 (엑셀 테이블 렌더링용)
     * 지표마스터, 부서 한 번에 fetch
     */
    @Query("""
        SELECT v FROM EsiIndicatorValue v
        JOIN FETCH v.indicator i
        JOIN FETCH v.department d
        WHERE v.baseYear = :year
          AND i.esgCategory = :category
          AND i.useYn = true
        ORDER BY i.sortOrder, d.sortOrder
        """)
    List<EsiIndicatorValue> findByYearAndCategory(
            @Param("year") Integer year,
            @Param("category") EsgCategory category);

    /**
     * 연도 + 부서별 지표값 조회
     */
    @Query("""
        SELECT v FROM EsiIndicatorValue v
        JOIN FETCH v.indicator i
        WHERE v.baseYear = :year AND v.department.deptId = :deptId
        ORDER BY i.esgCategory, i.sortOrder
        """)
    List<EsiIndicatorValue> findByYearAndDept(
            @Param("year") Integer year,
            @Param("deptId") Long deptId);

    /**
     * 지표 + 부서 + 연도 단건 조회 (UPSERT 용)
     */
    @Query("""
        SELECT v FROM EsiIndicatorValue v
        WHERE v.indicator.indicatorId = :indicatorId
          AND v.department.deptId = :deptId
          AND v.baseYear = :year
        """)
    Optional<EsiIndicatorValue> findByIndicatorAndDeptAndYear(
            @Param("indicatorId") Long indicatorId,
            @Param("deptId") Long deptId,
            @Param("year") Integer year);

    /**
     * 연도별 입력 현황 집계 (미입력 건수 포함)
     */
    @Query("""
        SELECT v.inputStatus, COUNT(v)
        FROM EsiIndicatorValue v
        JOIN v.indicator i
        WHERE v.baseYear = :year AND i.esgCategory = :category
        GROUP BY v.inputStatus
        """)
    List<Object[]> countByStatusAndYearAndCategory(
            @Param("year") Integer year,
            @Param("category") EsgCategory category);

    /**
     * 연도별 부서별 입력 현황
     */
    @Query("""
        SELECT d.deptId, d.deptName, v.inputStatus, COUNT(v)
        FROM EsiIndicatorValue v
        JOIN v.department d
        JOIN v.indicator i
        WHERE v.baseYear = :year AND i.esgCategory = :category
        GROUP BY d.deptId, d.deptName, v.inputStatus
        ORDER BY d.sortOrder
        """)
    List<Object[]> countByDeptAndStatus(
            @Param("year") Integer year,
            @Param("category") EsgCategory category);

    /**
     * 연도 목록 조회 (드롭다운용)
     */
    @Query("SELECT DISTINCT v.baseYear FROM EsiIndicatorValue v ORDER BY v.baseYear DESC")
    List<Integer> findDistinctYears();

    /**
     * Fact Book export 용 전체 데이터
     */
    @Query("""
        SELECT v FROM EsiIndicatorValue v
        JOIN FETCH v.indicator i
        JOIN FETCH v.department d
        WHERE v.baseYear = :year
          AND i.useYn = true
        ORDER BY i.esgCategory, i.sortOrder, d.sortOrder
        """)
    List<EsiIndicatorValue> findAllForExport(@Param("year") Integer year);

    /**
     * 확정되지 않은 제출 완료 건 (관리자 확정 화면용)
     */
    @Query("""
        SELECT v FROM EsiIndicatorValue v
        JOIN FETCH v.indicator i
        JOIN FETCH v.department d
        WHERE v.baseYear = :year
          AND v.inputStatus = :status
        ORDER BY i.esgCategory, i.sortOrder
        """)
    List<EsiIndicatorValue> findByYearAndStatus(
            @Param("year") Integer year,
            @Param("status") InputStatus status);
}
