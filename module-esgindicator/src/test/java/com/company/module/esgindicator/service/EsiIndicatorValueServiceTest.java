package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.request.ValueSaveRequest;
import com.company.module.esgindicator.dto.response.IndicatorValueResponse;
import com.company.module.esgindicator.entity.*;
import com.company.module.esgindicator.exception.EsiException;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import com.company.module.esgindicator.repository.EsiIndicatorHistoryRepository;
import com.company.module.esgindicator.repository.EsiIndicatorValueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * EsiIndicatorValueService 단위 테스트 (Mockito)
 */
@ExtendWith(MockitoExtension.class)
class EsiIndicatorValueServiceTest {

    @Mock EsiIndicatorValueRepository   valueRepository;
    @Mock EsiIndicatorHistoryRepository historyRepository;
    @Mock EsiDepartmentRepository       deptRepository;
    @Mock EsiDepartmentService          deptService;
    @Mock EsiIndicatorService           indicatorService;

    @InjectMocks
    EsiIndicatorValueService valueService;

    @Test
    @DisplayName("신규 지표값 저장 - NOT_STARTED → IN_PROGRESS")
    void saveValue_newRecord() throws Exception {
        // given
        EsiDepartment dept = EsiDepartment.builder()
                .deptCode("ENV").deptName("환경팀").useYn(true).sortOrder(1).build();
        EsiIndicator indicator = EsiIndicator.builder()
                .esgCategory(EsgCategory.E).minorCategory("온실가스").unit("tCO2eq")
                .sortOrder(1).useYn(true).build();

        // set IDs via reflection
        setField(dept, "deptId", 1L);
        setField(indicator, "indicatorId", 1L);

        ValueSaveRequest req = makeValueRequest(1L, 1L, 2024, BigDecimal.valueOf(315306), "계산값");

        given(indicatorService.findById(1L)).willReturn(indicator);
        given(deptService.findDeptById(1L)).willReturn(dept);
        given(valueRepository.findByIndicatorAndDeptAndYear(1L, 1L, 2024)).willReturn(Optional.empty());

        EsiIndicatorValue newValue = EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build();
        given(valueRepository.save(any(EsiIndicatorValue.class))).willReturn(newValue);

        // when
        IndicatorValueResponse response = valueService.saveValue(req);

        // then
        assertThat(response).isNotNull();
        verify(historyRepository).save(any());
    }

    @Test
    @DisplayName("확정된 지표값 수정 시 예외")
    void saveValue_alreadyConfirmed_throwsException() throws Exception {
        // given
        EsiDepartment dept = EsiDepartment.builder()
                .deptCode("ENV").deptName("환경팀").useYn(true).sortOrder(1).build();
        EsiIndicator indicator = EsiIndicator.builder()
                .esgCategory(EsgCategory.E).minorCategory("온실가스").unit("tCO2eq")
                .sortOrder(1).useYn(true).build();
        setField(dept, "deptId", 1L);
        setField(indicator, "indicatorId", 1L);

        EsiIndicatorValue confirmedValue = EsiIndicatorValue.builder()
                .indicator(indicator).department(dept).baseYear(2024).build();
        confirmedValue.updateValue(BigDecimal.valueOf(100), null);
        confirmedValue.submit();
        confirmedValue.confirm(null);

        ValueSaveRequest req = makeValueRequest(1L, 1L, 2024, BigDecimal.valueOf(999), null);

        given(indicatorService.findById(1L)).willReturn(indicator);
        given(deptService.findDeptById(1L)).willReturn(dept);
        given(valueRepository.findByIndicatorAndDeptAndYear(1L, 1L, 2024))
                .willReturn(Optional.of(confirmedValue));

        // when & then
        assertThatThrownBy(() -> valueService.saveValue(req))
                .isInstanceOf(EsiException.class);
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────────────

    private ValueSaveRequest makeValueRequest(Long indicatorId, Long deptId,
                                              Integer year, BigDecimal val, String remark) {
        try {
            ValueSaveRequest req = new ValueSaveRequest();
            setField(req, "indicatorId", indicatorId);
            setField(req, "deptId", deptId);
            setField(req, "baseYear", year);
            setField(req, "indicatorValue", val);
            setField(req, "formulaRemark", remark);
            return req;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = findField(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), fieldName);
            throw new RuntimeException(e);
        }
    }
}
