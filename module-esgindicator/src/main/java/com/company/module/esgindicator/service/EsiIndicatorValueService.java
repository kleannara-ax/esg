package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.request.ValueBulkSaveRequest;
import com.company.module.esgindicator.dto.request.ValueConfirmRequest;
import com.company.module.esgindicator.dto.request.ValueSaveRequest;
import com.company.module.esgindicator.dto.response.IndicatorSheetResponse;
import com.company.module.esgindicator.dto.response.IndicatorValueResponse;
import com.company.module.esgindicator.dto.response.InputStatusSummaryResponse;
import com.company.module.esgindicator.entity.*;
import com.company.module.esgindicator.exception.EsiException;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import com.company.module.esgindicator.repository.EsiIndicatorHistoryRepository;
import com.company.module.esgindicator.repository.EsiIndicatorValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 연도별 ESG 지표값 서비스
 * - 입력/수정/제출/확정 상태 전이
 * - 화면용 시트 데이터 조합
 * - 이력 자동 기록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiIndicatorValueService {

    private final EsiIndicatorValueRepository   valueRepository;
    private final EsiIndicatorHistoryRepository historyRepository;
    private final EsiDepartmentRepository       deptRepository;
    private final EsiDepartmentService          deptService;
    private final EsiIndicatorService           indicatorService;

    // ── 화면용 시트 조회 ──────────────────────────────────────────────────────

    /**
     * 연도 + ESG구분으로 엑셀 시트 형태 데이터 조회
     * 행: 지표, 열: 부서
     */
    @Transactional(readOnly = true)
    public IndicatorSheetResponse getSheet(Integer year, EsgCategory category) {
        List<EsiDepartment> activeDepts = deptRepository.findAllActive();
        List<EsiIndicatorValue> values  = valueRepository.findByYearAndCategory(year, category);

        // 지표별, 부서별로 값 맵핑
        Map<Long, Map<Long, EsiIndicatorValue>> indicatorDeptMap = new LinkedHashMap<>();
        for (EsiIndicatorValue v : values) {
            indicatorDeptMap
                .computeIfAbsent(v.getIndicator().getIndicatorId(), k -> new LinkedHashMap<>())
                .put(v.getDepartment().getDeptId(), v);
        }

        // 행(Row) 구성
        List<IndicatorSheetResponse.IndicatorRowResponse> rows = new ArrayList<>();
        // 사용중인 지표 목록(sortOrder 기준)
        indicatorService.getIndicatorsByCategory(category).forEach(indResp -> {
            List<IndicatorValueResponse> cellValues = activeDepts.stream()
                .map(dept -> {
                    Map<Long, EsiIndicatorValue> deptMap =
                        indicatorDeptMap.getOrDefault(indResp.getIndicatorId(), Map.of());
                    EsiIndicatorValue v = deptMap.get(dept.getDeptId());
                    return v != null ? IndicatorValueResponse.from(v) : null;
                })
                .toList();

            rows.add(IndicatorSheetResponse.IndicatorRowResponse.builder()
                .indicatorId(indResp.getIndicatorId())
                .majorCategory(indResp.getMajorCategory())
                .midCategory(indResp.getMidCategory())
                .minorCategory(indResp.getMinorCategory())
                .unit(indResp.getUnit())
                .sortOrder(indResp.getSortOrder())
                .values(cellValues)
                .build());
        });

        // 부서 DTO 변환
        List<com.company.module.esgindicator.dto.response.DepartmentResponse> deptResponses =
            activeDepts.stream()
                .map(com.company.module.esgindicator.dto.response.DepartmentResponse::from)
                .toList();

        InputStatusSummaryResponse summary = buildSummary(year, category, values, activeDepts);

        return IndicatorSheetResponse.builder()
                .baseYear(year)
                .esgCategory(category.name())
                .esgCategoryName(category.getKorName())
                .departments(deptResponses)
                .rows(rows)
                .summary(summary)
                .build();
    }

    // ── 값 저장 (단건 UPSERT) ─────────────────────────────────────────────────

    @Transactional
    public IndicatorValueResponse saveValue(ValueSaveRequest request) {
        EsiIndicator   indicator = indicatorService.findById(request.getIndicatorId());
        EsiDepartment  dept      = deptService.findDeptById(request.getDeptId());

        Optional<EsiIndicatorValue> optValue = valueRepository.findByIndicatorAndDeptAndYear(
                request.getIndicatorId(), request.getDeptId(), request.getBaseYear());

        EsiIndicatorValue value;
        String beforeStatus;
        BigDecimal beforeValue;

        if (optValue.isPresent()) {
            value = optValue.get();
            // 확정된 경우 수정 불가
            if (value.isConfirmedYn()) throw EsiException.alreadyConfirmed(value.getValueId());

            beforeStatus = value.getInputStatus().name();
            beforeValue  = value.getIndicatorValue();
            value.updateValue(request.getIndicatorValue(), request.getFormulaRemark());
        } else {
            beforeStatus = null;
            beforeValue  = null;
            value = EsiIndicatorValue.builder()
                    .indicator(indicator)
                    .department(dept)
                    .baseYear(request.getBaseYear())
                    .build();
            value = valueRepository.save(value);
            value.updateValue(request.getIndicatorValue(), request.getFormulaRemark());
        }

        // 이력 기록
        saveHistory(value, "UPDATE", beforeValue, request.getIndicatorValue(),
                    beforeStatus, value.getInputStatus().name(), null);

        log.info("[ESI] 지표값 저장: indicatorId={}, deptId={}, year={}, value={}",
                 request.getIndicatorId(), request.getDeptId(),
                 request.getBaseYear(), request.getIndicatorValue());
        return IndicatorValueResponse.from(value);
    }

    // ── 일괄 저장 ─────────────────────────────────────────────────────────────

    @Transactional
    public List<IndicatorValueResponse> bulkSaveValues(ValueBulkSaveRequest request) {
        EsiDepartment dept = deptService.findDeptById(request.getDeptId());

        List<IndicatorValueResponse> results = new ArrayList<>();
        for (ValueBulkSaveRequest.ValueItemRequest item : request.getItems()) {
            ValueSaveRequest single = new ValueSaveRequest();
            // reflection 없이 직접 필드 사용하기 위해 별도 처리
            results.add(saveValueInternal(
                    item.getIndicatorId(),
                    dept,
                    request.getBaseYear(),
                    item.getIndicatorValue(),
                    item.getFormulaRemark()
            ));
        }
        log.info("[ESI] 일괄 저장: deptId={}, year={}, count={}", request.getDeptId(),
                  request.getBaseYear(), results.size());
        return results;
    }

    private IndicatorValueResponse saveValueInternal(
            Long indicatorId, EsiDepartment dept,
            Integer year, BigDecimal value, String remark) {

        EsiIndicator indicator = indicatorService.findById(indicatorId);

        Optional<EsiIndicatorValue> optValue =
            valueRepository.findByIndicatorAndDeptAndYear(indicatorId, dept.getDeptId(), year);

        EsiIndicatorValue entity;
        String beforeStatus;
        BigDecimal beforeValue;

        if (optValue.isPresent()) {
            entity = optValue.get();
            if (entity.isConfirmedYn()) throw EsiException.alreadyConfirmed(entity.getValueId());
            beforeStatus = entity.getInputStatus().name();
            beforeValue  = entity.getIndicatorValue();
            entity.updateValue(value, remark);
        } else {
            beforeStatus = null;
            beforeValue  = null;
            entity = valueRepository.save(EsiIndicatorValue.builder()
                    .indicator(indicator)
                    .department(dept)
                    .baseYear(year)
                    .build());
            entity.updateValue(value, remark);
        }

        saveHistory(entity, "UPDATE", beforeValue, value,
                    beforeStatus, entity.getInputStatus().name(), null);
        return IndicatorValueResponse.from(entity);
    }

    // ── 제출 ──────────────────────────────────────────────────────────────────

    @Transactional
    public IndicatorValueResponse submitValue(Long valueId) {
        EsiIndicatorValue value = findValueById(valueId);
        String beforeStatus = value.getInputStatus().name();
        value.submit();
        saveHistory(value, "SUBMIT", value.getIndicatorValue(), value.getIndicatorValue(),
                    beforeStatus, value.getInputStatus().name(), null);
        log.info("[ESI] 지표값 제출: valueId={}", valueId);
        return IndicatorValueResponse.from(value);
    }

    // ── 확정 / 확정취소 ────────────────────────────────────────────────────────

    @Transactional
    public IndicatorValueResponse confirmValue(Long valueId, ValueConfirmRequest request) {
        EsiIndicatorValue value = findValueById(valueId);
        String beforeStatus = value.getInputStatus().name();
        value.confirm(request.getConfirmRemark());
        saveHistory(value, "CONFIRM", value.getIndicatorValue(), value.getIndicatorValue(),
                    beforeStatus, value.getInputStatus().name(), request.getConfirmRemark());
        log.info("[ESI] 지표값 최종확정: valueId={}", valueId);
        return IndicatorValueResponse.from(value);
    }

    @Transactional
    public IndicatorValueResponse cancelConfirmValue(Long valueId) {
        EsiIndicatorValue value = findValueById(valueId);
        String beforeStatus = value.getInputStatus().name();
        value.cancelConfirm();
        saveHistory(value, "CANCEL", value.getIndicatorValue(), value.getIndicatorValue(),
                    beforeStatus, value.getInputStatus().name(), null);
        log.info("[ESI] 지표값 확정취소: valueId={}", valueId);
        return IndicatorValueResponse.from(value);
    }

    // ── 연도 목록 조회 ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears() {
        return valueRepository.findDistinctYears();
    }

    // ── Fact Book Export 데이터 ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IndicatorValueResponse> getExportData(Integer year) {
        return valueRepository.findAllForExport(year)
                .stream()
                .map(IndicatorValueResponse::from)
                .toList();
    }

    // ── 입력 현황 요약 ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InputStatusSummaryResponse getInputSummary(Integer year, EsgCategory category) {
        List<EsiDepartment> activeDepts = deptRepository.findAllActive();
        List<EsiIndicatorValue> values  = valueRepository.findByYearAndCategory(year, category);
        return buildSummary(year, category, values, activeDepts);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────────

    private EsiIndicatorValue findValueById(Long valueId) {
        return valueRepository.findById(valueId)
                .orElseThrow(() -> EsiException.valueNotFound(valueId));
    }

    private void saveHistory(EsiIndicatorValue value, String changeType,
                              BigDecimal beforeValue, BigDecimal afterValue,
                              String beforeStatus, String afterStatus, String remark) {
        String currentUser = getCurrentUsername();
        EsiIndicatorHistory history = EsiIndicatorHistory.of(
                value, changeType, beforeValue, afterValue,
                beforeStatus, afterStatus, remark, currentUser);
        historyRepository.save(history);
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private InputStatusSummaryResponse buildSummary(
            Integer year, EsgCategory category,
            List<EsiIndicatorValue> values, List<EsiDepartment> activeDepts) {

        long total        = values.size();
        long notStarted   = values.stream().filter(v -> v.getInputStatus() == InputStatus.NOT_STARTED).count();
        long inProgress   = values.stream().filter(v -> v.getInputStatus() == InputStatus.IN_PROGRESS).count();
        long submitted    = values.stream().filter(v -> v.getInputStatus() == InputStatus.SUBMITTED).count();
        long confirmed    = values.stream().filter(v -> v.getInputStatus() == InputStatus.CONFIRMED).count();

        return InputStatusSummaryResponse.builder()
                .baseYear(year)
                .esgCategory(category.name())
                .esgCategoryName(category.getKorName())
                .totalCount(total)
                .notStartedCount(notStarted)
                .inProgressCount(inProgress)
                .submittedCount(submitted)
                .confirmedCount(confirmed)
                .build();
    }
}
