package com.company.module.esg.repository;

import com.company.module.esg.config.TestJpaConfig;
import com.company.module.esg.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EsgDataEntryRepository 슬라이스 테스트 (H2 인메모리)
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
@DisplayName("EsgDataEntryRepository 테스트")
class EsgDataEntryRepositoryTest {

    @Autowired
    private EsgDataEntryRepository dataEntryRepository;

    @Autowired
    private EsgIndicatorRepository indicatorRepository;

    private EsgIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = indicatorRepository.save(EsgIndicator.builder()
                .category(EsgCategory.E)
                .indicatorCode("E_GHG_001")
                .indicatorName("온실가스 테스트")
                .unit("tCO2eq")
                .activeYn(true)
                .build());
    }

    @Test
    @DisplayName("데이터 저장 및 연월 조회")
    void saveAndFindByYearMonth() {
        // given
        EsgDataEntry entry = EsgDataEntry.builder()
                .indicator(indicator)
                .targetYear(2024)
                .targetMonth(1)
                .actualValue(new BigDecimal("1250.5"))
                .targetValue(new BigDecimal("1200.0"))
                .entryStatus(EntryStatus.DRAFT)
                .build();
        dataEntryRepository.save(entry);

        // when
        List<EsgDataEntry> result = dataEntryRepository.findByTargetYearAndTargetMonth(2024, 1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActualValue()).isEqualByComparingTo(new BigDecimal("1250.5"));
    }

    @Test
    @DisplayName("지표ID + 연월 중복 조회")
    void findByIndicatorIdAndYearMonth() {
        // given
        dataEntryRepository.save(buildEntry(2024, 6, EntryStatus.SUBMITTED));

        // when
        Optional<EsgDataEntry> found = dataEntryRepository
                .findByIndicator_IndicatorIdAndTargetYearAndTargetMonth(
                        indicator.getIndicatorId(), 2024, 6);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEntryStatus()).isEqualTo(EntryStatus.SUBMITTED);
    }

    @Test
    @DisplayName("연간 추이 조회 - 월 순서 정렬 확인")
    void findAnnualTrend_sorted() {
        // given
        dataEntryRepository.save(buildEntry(2024, 3, EntryStatus.APPROVED));
        dataEntryRepository.save(buildEntry(2024, 1, EntryStatus.APPROVED));
        dataEntryRepository.save(buildEntry(2024, 2, EntryStatus.APPROVED));

        // when
        List<EsgDataEntry> trend = dataEntryRepository.findAnnualTrend(
                indicator.getIndicatorId(), 2024);

        // then
        assertThat(trend).hasSize(3);
        assertThat(trend.get(0).getTargetMonth()).isEqualTo(1);
        assertThat(trend.get(1).getTargetMonth()).isEqualTo(2);
        assertThat(trend.get(2).getTargetMonth()).isEqualTo(3);
    }

    @Test
    @DisplayName("월별 리포트 조회 - 상태 필터링")
    void findMonthlyReport_withStatusFilter() {
        // given
        dataEntryRepository.save(buildEntry(2024, 4, EntryStatus.SUBMITTED));
        dataEntryRepository.save(buildEntry(2024, 5, EntryStatus.APPROVED));

        // when
        List<EsgDataEntry> approved = dataEntryRepository.findMonthlyReport(2024, 5, EntryStatus.APPROVED);
        List<EsgDataEntry> all      = dataEntryRepository.findMonthlyReport(2024, 4, null);

        // then
        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getEntryStatus()).isEqualTo(EntryStatus.APPROVED);
        assertThat(all).hasSize(1);
    }

    private EsgDataEntry buildEntry(int year, int month, EntryStatus status) {
        return EsgDataEntry.builder()
                .indicator(indicator)
                .targetYear(year)
                .targetMonth(month)
                .actualValue(new BigDecimal("1000.0"))
                .targetValue(new BigDecimal("1200.0"))
                .entryStatus(status)
                .build();
    }
}
