package com.company.module.esg.repository;

import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgIndicator;
import com.company.module.esg.config.TestJpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EsgIndicatorRepository 슬라이스 테스트 (H2 인메모리)
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
@DisplayName("EsgIndicatorRepository 테스트")
class EsgIndicatorRepositoryTest {

    @Autowired
    private EsgIndicatorRepository indicatorRepository;

    @Test
    @DisplayName("지표 저장 및 코드로 조회")
    void saveAndFindByCode() {
        // given
        EsgIndicator indicator = EsgIndicator.builder()
                .category(EsgCategory.E)
                .indicatorCode("E_TEST_001")
                .indicatorName("테스트 지표")
                .unit("tCO2eq")
                .activeYn(true)
                .build();

        // when
        indicatorRepository.save(indicator);
        Optional<EsgIndicator> found = indicatorRepository.findByIndicatorCode("E_TEST_001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getIndicatorName()).isEqualTo("테스트 지표");
        assertThat(found.get().getCategory()).isEqualTo(EsgCategory.E);
    }

    @Test
    @DisplayName("카테고리별 활성 지표 조회")
    void findByCategoryAndActiveYnTrue() {
        // given
        indicatorRepository.save(buildIndicator("E_001", EsgCategory.E, true));
        indicatorRepository.save(buildIndicator("E_002", EsgCategory.E, false));  // 비활성
        indicatorRepository.save(buildIndicator("S_001", EsgCategory.S, true));

        // when
        List<EsgIndicator> envIndicators =
                indicatorRepository.findByCategoryAndActiveYnTrue(EsgCategory.E);

        // then
        assertThat(envIndicators).hasSize(1);
        assertThat(envIndicators.get(0).getIndicatorCode()).isEqualTo("E_001");
    }

    @Test
    @DisplayName("중복 코드 존재 여부 확인")
    void existsByIndicatorCode() {
        // given
        indicatorRepository.save(buildIndicator("E_EXIST_001", EsgCategory.E, true));

        // when & then
        assertThat(indicatorRepository.existsByIndicatorCode("E_EXIST_001")).isTrue();
        assertThat(indicatorRepository.existsByIndicatorCode("E_NOT_EXIST")).isFalse();
    }

    @Test
    @DisplayName("전체 활성 지표 조회")
    void findAllByActiveYnTrue() {
        // given
        indicatorRepository.save(buildIndicator("E_ACT_001", EsgCategory.E, true));
        indicatorRepository.save(buildIndicator("E_ACT_002", EsgCategory.E, true));
        indicatorRepository.save(buildIndicator("S_INA_001", EsgCategory.S, false));

        // when
        List<EsgIndicator> active = indicatorRepository.findAllByActiveYnTrue();

        // then
        assertThat(active).hasSize(2);
        assertThat(active).allMatch(EsgIndicator::isActiveYn);
    }

    private EsgIndicator buildIndicator(String code, EsgCategory category, boolean active) {
        return EsgIndicator.builder()
                .category(category)
                .indicatorCode(code)
                .indicatorName("테스트 " + code)
                .unit("단위")
                .activeYn(active)
                .build();
    }
}
