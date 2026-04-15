package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.config.TestJpaConfig;
import com.company.module.esgindicator.entity.*;
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
 * EsiIndicatorValueRepository H2 슬라이스 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class EsiIndicatorValueRepositoryTest {

    @Autowired EsiDepartmentRepository    deptRepository;
    @Autowired EsiIndicatorRepository     indicatorRepository;
    @Autowired EsiIndicatorValueRepository valueRepository;

    EsiDepartment dept;
    EsiIndicator  indicator;

    @BeforeEach
    void setUp() {
        dept = deptRepository.save(EsiDepartment.builder()
                .deptCode("ENV").deptName("환경부서").useYn(true).sortOrder(1).build());

        indicator = indicatorRepository.save(EsiIndicator.builder()
                .esgCategory(EsgCategory.E)
                .minorCategory("온실가스 총배출량")
                .unit("tCO2eq")
                .sortOrder(1)
                .useYn(true)
                .build());
    }

    @Test
    @DisplayName("지표값 저장 후 조회")
    void saveAndFind() {
        // given
        EsiIndicatorValue value = EsiIndicatorValue.builder()
                .indicator(indicator)
                .department(dept)
                .baseYear(2024)
                .build();
        value.updateValue(BigDecimal.valueOf(315306), "Scope1+2 합산");
        EsiIndicatorValue saved = valueRepository.save(value);

        // when
        Optional<EsiIndicatorValue> found = valueRepository.findByIndicatorAndDeptAndYear(
                indicator.getIndicatorId(), dept.getDeptId(), 2024);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getIndicatorValue()).isEqualByComparingTo(BigDecimal.valueOf(315306));
        assertThat(found.get().getInputStatus()).isEqualTo(InputStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("제출 상태 전이")
    void submitStatusTransition() {
        // given
        EsiIndicatorValue value = valueRepository.save(EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build());
        value.updateValue(BigDecimal.valueOf(100), null);

        // when
        value.submit();

        // then
        assertThat(value.getInputStatus()).isEqualTo(InputStatus.SUBMITTED);
    }

    @Test
    @DisplayName("연도별 카테고리 조회")
    void findByYearAndCategory() {
        // given
        EsiIndicatorValue v = valueRepository.save(EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build());
        v.updateValue(BigDecimal.valueOf(200), null);

        // when
        List<EsiIndicatorValue> result = valueRepository.findByYearAndCategory(2024, EsgCategory.E);

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("확정 처리 및 취소")
    void confirmAndCancel() {
        // given
        EsiIndicatorValue value = valueRepository.save(EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build());
        value.updateValue(BigDecimal.valueOf(300), null);
        value.submit();

        // when: 확정
        value.confirm("검토 완료");

        // then
        assertThat(value.isConfirmedYn()).isTrue();
        assertThat(value.getInputStatus()).isEqualTo(InputStatus.CONFIRMED);

        // when: 확정 취소
        value.cancelConfirm();

        // then
        assertThat(value.isConfirmedYn()).isFalse();
        assertThat(value.getInputStatus()).isEqualTo(InputStatus.SUBMITTED);
    }

    @Test
    @DisplayName("연도 목록 조회")
    void findDistinctYears() {
        // given
        EsiIndicatorValue v2024 = valueRepository.save(EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build());
        v2024.updateValue(BigDecimal.ONE, null);

        // when
        List<Integer> years = valueRepository.findDistinctYears();

        // then
        assertThat(years).contains(2024);
    }
}
