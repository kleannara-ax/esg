package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.config.TestJpaConfig;
import com.company.module.esgindicator.entity.EsiDepartment;
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
 * EsiDepartmentRepository H2 슬라이스 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class EsiDepartmentRepositoryTest {

    @Autowired
    private EsiDepartmentRepository deptRepository;

    @Test
    @DisplayName("부서 저장 및 단건 조회")
    void saveAndFind() {
        // given
        EsiDepartment dept = EsiDepartment.builder()
                .deptCode("TEST_DEPT")
                .deptName("테스트부서")
                .useYn(true)
                .sortOrder(1)
                .build();

        // when
        EsiDepartment saved = deptRepository.save(dept);

        // then
        assertThat(saved.getDeptId()).isNotNull();
        assertThat(saved.getDeptCode()).isEqualTo("TEST_DEPT");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("활성 부서만 조회 - useYn=true 만 반환")
    void findAllActive() {
        // given
        EsiDepartment active = EsiDepartment.builder()
                .deptCode("ACTIVE").deptName("활성부서").useYn(true).sortOrder(1).build();
        EsiDepartment inactive = EsiDepartment.builder()
                .deptCode("INACTIVE").deptName("비활성부서").useYn(false).sortOrder(2).build();
        deptRepository.saveAll(List.of(active, inactive));

        // when
        List<EsiDepartment> result = deptRepository.findAllActive();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptCode()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("부서 코드로 조회")
    void findByDeptCode() {
        // given
        deptRepository.save(EsiDepartment.builder()
                .deptCode("HR").deptName("인사팀").useYn(true).sortOrder(1).build());

        // when
        Optional<EsiDepartment> result = deptRepository.findByDeptCode("HR");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDeptName()).isEqualTo("인사팀");
    }

    @Test
    @DisplayName("부서 코드 중복 확인")
    void existsByDeptCode() {
        // given
        deptRepository.save(EsiDepartment.builder()
                .deptCode("EXISTS").deptName("기존부서").useYn(true).sortOrder(1).build());

        // then
        assertThat(deptRepository.existsByDeptCode("EXISTS")).isTrue();
        assertThat(deptRepository.existsByDeptCode("NEW")).isFalse();
    }
}
