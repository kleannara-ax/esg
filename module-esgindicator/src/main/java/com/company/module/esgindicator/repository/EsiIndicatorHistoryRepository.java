package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.entity.EsiIndicatorHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ESG 지표값 변경 이력 Repository
 */
public interface EsiIndicatorHistoryRepository extends JpaRepository<EsiIndicatorHistory, Long> {

    /**
     * 지표값 ID 로 이력 목록 조회 (최신순)
     */
    @Query("""
        SELECT h FROM EsiIndicatorHistory h
        WHERE h.indicatorValue.valueId = :valueId
        ORDER BY h.changedAt DESC
        """)
    List<EsiIndicatorHistory> findByValueId(@Param("valueId") Long valueId);

    /**
     * 연도 + 지표ID 로 이력 조회
     */
    @Query("""
        SELECT h FROM EsiIndicatorHistory h
        JOIN h.indicatorValue v
        WHERE v.baseYear = :year AND v.indicator.indicatorId = :indicatorId
        ORDER BY h.changedAt DESC
        """)
    List<EsiIndicatorHistory> findByYearAndIndicatorId(
            @Param("year") Integer year,
            @Param("indicatorId") Long indicatorId);
}
