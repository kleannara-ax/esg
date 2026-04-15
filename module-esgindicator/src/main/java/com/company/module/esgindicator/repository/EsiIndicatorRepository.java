package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.entity.EsiIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ESG 지표 마스터 Repository
 */
public interface EsiIndicatorRepository extends JpaRepository<EsiIndicator, Long> {

    /**
     * ESG 구분별 사용중인 지표 목록 (정렬순서 오름차순)
     */
    @Query("""
        SELECT i FROM EsiIndicator i
        LEFT JOIN FETCH i.department
        WHERE i.esgCategory = :category AND i.useYn = true
        ORDER BY i.sortOrder, i.indicatorId
        """)
    List<EsiIndicator> findByCategoryActive(@Param("category") EsgCategory category);

    /**
     * 사용중인 전체 지표 목록
     */
    @Query("""
        SELECT i FROM EsiIndicator i
        LEFT JOIN FETCH i.department
        WHERE i.useYn = true
        ORDER BY i.esgCategory, i.sortOrder
        """)
    List<EsiIndicator> findAllActive();

    /**
     * ESG 구분별 전체 지표 목록 (관리자용, 비활성 포함)
     */
    @Query("""
        SELECT i FROM EsiIndicator i
        LEFT JOIN FETCH i.department
        WHERE i.esgCategory = :category
        ORDER BY i.sortOrder, i.indicatorId
        """)
    List<EsiIndicator> findByCategory(@Param("category") EsgCategory category);

    /**
     * 엑셀 행번호로 지표 조회 (엑셀 업로드 매핑용)
     */
    @Query("""
        SELECT i FROM EsiIndicator i
        WHERE i.esgCategory = :category AND i.excelRowNum = :rowNum
        """)
    java.util.Optional<EsiIndicator> findByExcelRowNum(
            @Param("category") EsgCategory category,
            @Param("rowNum") Integer rowNum);
}
