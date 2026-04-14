package com.company.module.esg.repository;

import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ESG 지표 Repository
 */
public interface EsgIndicatorRepository extends JpaRepository<EsgIndicator, Long> {

    List<EsgIndicator> findByCategoryAndActiveYnTrue(EsgCategory category);

    List<EsgIndicator> findAllByActiveYnTrue();

    Optional<EsgIndicator> findByIndicatorCode(String indicatorCode);

    boolean existsByIndicatorCode(String indicatorCode);

    @Query("""
        SELECT i FROM EsgIndicator i
        WHERE (:category IS NULL OR i.category = :category)
          AND i.activeYn = true
        ORDER BY i.category, i.indicatorCode
        """)
    List<EsgIndicator> findByOptionalCategory(@Param("category") EsgCategory category);
}
