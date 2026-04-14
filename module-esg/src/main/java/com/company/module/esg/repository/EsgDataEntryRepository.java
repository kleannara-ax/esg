package com.company.module.esg.repository;

import com.company.module.esg.entity.EsgDataEntry;
import com.company.module.esg.entity.EntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ESG 데이터 입력 Repository
 */
public interface EsgDataEntryRepository extends JpaRepository<EsgDataEntry, Long> {

    List<EsgDataEntry> findByIndicator_IndicatorId(Long indicatorId);

    List<EsgDataEntry> findByTargetYearAndTargetMonth(Integer year, Integer month);

    List<EsgDataEntry> findByEntryStatus(EntryStatus status);

    Optional<EsgDataEntry> findByIndicator_IndicatorIdAndTargetYearAndTargetMonth(
            Long indicatorId, Integer year, Integer month);

    @Query("""
        SELECT e FROM EsgDataEntry e
        JOIN FETCH e.indicator i
        WHERE e.targetYear  = :year
          AND e.targetMonth = :month
          AND (:status IS NULL OR e.entryStatus = :status)
        ORDER BY i.category, i.indicatorCode
        """)
    List<EsgDataEntry> findMonthlyReport(
            @Param("year")   Integer year,
            @Param("month")  Integer month,
            @Param("status") EntryStatus status);

    @Query("""
        SELECT e FROM EsgDataEntry e
        JOIN FETCH e.indicator i
        WHERE i.indicatorId = :indicatorId
          AND e.targetYear  = :year
        ORDER BY e.targetMonth
        """)
    List<EsgDataEntry> findAnnualTrend(
            @Param("indicatorId") Long indicatorId,
            @Param("year")        Integer year);
}
