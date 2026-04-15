package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.request.DepartmentCreateRequest;
import com.company.module.esgindicator.dto.response.DepartmentResponse;
import com.company.module.esgindicator.entity.EsiDepartment;
import com.company.module.esgindicator.exception.EsiException;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * EsiDepartmentService 단위 테스트 (Mockito)
 */
@ExtendWith(MockitoExtension.class)
class EsiDepartmentServiceTest {

    @Mock
    EsiDepartmentRepository deptRepository;

    @InjectMocks
    EsiDepartmentService deptService;

    @Test
    @DisplayName("활성 부서 목록 조회")
    void getActiveDepartments() {
        // given
        EsiDepartment dept = EsiDepartment.builder()
                .deptCode("TEST").deptName("테스트팀").useYn(true).sortOrder(1).build();
        given(deptRepository.findAllActive()).willReturn(List.of(dept));

        // when
        List<DepartmentResponse> result = deptService.getActiveDepartments();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptCode()).isEqualTo("TEST");
    }

    @Test
    @DisplayName("부서 등록 - 정상")
    void createDepartment_success() throws Exception {
        // given
        DepartmentCreateRequest req = makeRequest("HR", "인사팀", 1, true);
        given(deptRepository.existsByDeptCode("HR")).willReturn(false);

        EsiDepartment saved = EsiDepartment.builder()
                .deptCode("HR").deptName("인사팀").useYn(true).sortOrder(1).build();
        given(deptRepository.save(any())).willReturn(saved);

        // when
        DepartmentResponse response = deptService.createDepartment(req);

        // then
        assertThat(response.getDeptCode()).isEqualTo("HR");
        assertThat(response.getDeptName()).isEqualTo("인사팀");
    }

    @Test
    @DisplayName("부서 등록 - 코드 중복 예외")
    void createDepartment_duplicateCode() {
        // given
        DepartmentCreateRequest req = makeRequest("DUPLICATE", "중복팀", 1, true);
        given(deptRepository.existsByDeptCode("DUPLICATE")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> deptService.createDepartment(req))
                .isInstanceOf(EsiException.class);
    }

    @Test
    @DisplayName("부서 조회 - 존재하지 않는 ID 예외")
    void getDepartment_notFound() {
        // given
        given(deptRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deptService.getDepartment(999L))
                .isInstanceOf(EsiException.class);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private DepartmentCreateRequest makeRequest(String code, String name, int sort, boolean useYn) {
        try {
            DepartmentCreateRequest req = new DepartmentCreateRequest();
            // Reflection for no-args + field access (Lombok)
            var f1 = DepartmentCreateRequest.class.getDeclaredField("deptCode");
            var f2 = DepartmentCreateRequest.class.getDeclaredField("deptName");
            var f3 = DepartmentCreateRequest.class.getDeclaredField("sortOrder");
            var f4 = DepartmentCreateRequest.class.getDeclaredField("useYn");
            f1.setAccessible(true); f1.set(req, code);
            f2.setAccessible(true); f2.set(req, name);
            f3.setAccessible(true); f3.set(req, sort);
            f4.setAccessible(true); f4.set(req, useYn);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
