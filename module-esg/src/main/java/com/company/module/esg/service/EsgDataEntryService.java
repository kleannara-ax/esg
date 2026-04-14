package com.company.module.esg.service;

import com.company.module.esg.dto.request.EsgDataEntryRequest;
import com.company.module.esg.dto.response.EsgDataEntryResponse;
import com.company.module.esg.entity.EntryStatus;
import com.company.module.esg.entity.EsgDataEntry;
import com.company.module.esg.entity.EsgIndicator;
import com.company.module.esg.exception.EsgException;
import com.company.module.esg.repository.EsgDataEntryRepository;
import com.company.module.esg.repository.EsgIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ESG 데이터 입력 Service
 * <p>@Transactional 은 Service 계층에서만 사용</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsgDataEntryService {

    private final EsgDataEntryRepository dataEntryRepository;
    private final EsgIndicatorRepository indicatorRepository;

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** 월별 ESG 데이터 조회 */
    @Transactional(readOnly = true)
    public List<EsgDataEntryResponse> getMonthlyReport(int year, int month, EntryStatus status) {
        return dataEntryRepository.findMonthlyReport(year, month, status)
                .stream()
                .map(EsgDataEntryResponse::from)
                .toList();
    }

    /** 지표별 연간 추이 조회 */
    @Transactional(readOnly = true)
    public List<EsgDataEntryResponse> getAnnualTrend(Long indicatorId, int year) {
        return dataEntryRepository.findAnnualTrend(indicatorId, year)
                .stream()
                .map(EsgDataEntryResponse::from)
                .toList();
    }

    /** 데이터 단건 조회 */
    @Transactional(readOnly = true)
    public EsgDataEntryResponse getDataEntry(Long entryId) {
        return EsgDataEntryResponse.from(findById(entryId));
    }

    // ── 생성 ────────────────────────────────────────────────────────────────

    /** ESG 데이터 입력 */
    @Transactional
    public EsgDataEntryResponse createDataEntry(EsgDataEntryRequest request) {
        // 지표 존재 확인
        EsgIndicator indicator = indicatorRepository.findById(request.getIndicatorId())
                .orElseThrow(() -> EsgException.indicatorNotFound(request.getIndicatorId()));

        // 중복 입력 확인
        dataEntryRepository.findByIndicator_IndicatorIdAndTargetYearAndTargetMonth(
                        request.getIndicatorId(), request.getTargetYear(), request.getTargetMonth())
                .ifPresent(e -> {
                    throw EsgException.dataEntryDuplicated(
                            request.getIndicatorId(),
                            request.getTargetYear(),
                            request.getTargetMonth());
                });

        EsgDataEntry entry = EsgDataEntry.builder()
                .indicator(indicator)
                .targetYear(request.getTargetYear())
                .targetMonth(request.getTargetMonth())
                .actualValue(request.getActualValue())
                .targetValue(request.getTargetValue())
                .remark(request.getRemark())
                .entryStatus(EntryStatus.DRAFT)
                .build();

        EsgDataEntry saved = dataEntryRepository.save(entry);
        log.info("ESG 데이터 입력: indicatorId={}, {}년 {}월",
                indicator.getIndicatorId(), request.getTargetYear(), request.getTargetMonth());
        return EsgDataEntryResponse.from(saved);
    }

    // ── 수정 ────────────────────────────────────────────────────────────────

    /** ESG 데이터 수정 (DRAFT 상태에서만 가능) */
    @Transactional
    public EsgDataEntryResponse updateDataEntry(Long entryId, EsgDataEntryRequest request) {
        EsgDataEntry entry = findById(entryId);
        validateDraftStatus(entry);

        entry.updateValues(request.getActualValue(), request.getTargetValue(), request.getRemark());
        log.info("ESG 데이터 수정: entryId={}", entryId);
        return EsgDataEntryResponse.from(entry);
    }

    // ── 상태 전환 ────────────────────────────────────────────────────────────

    /** 제출 (DRAFT → SUBMITTED) */
    @Transactional
    public EsgDataEntryResponse submitDataEntry(Long entryId) {
        EsgDataEntry entry = findById(entryId);
        if (entry.getEntryStatus() != EntryStatus.DRAFT) {
            throw EsgException.invalidStatusTransition(
                    entry.getEntryStatus().name(), EntryStatus.SUBMITTED.name());
        }
        entry.submit();
        log.info("ESG 데이터 제출: entryId={}", entryId);
        return EsgDataEntryResponse.from(entry);
    }

    /** 승인 (SUBMITTED → APPROVED) */
    @Transactional
    public EsgDataEntryResponse approveDataEntry(Long entryId) {
        EsgDataEntry entry = findById(entryId);
        if (entry.getEntryStatus() != EntryStatus.SUBMITTED) {
            throw EsgException.invalidStatusTransition(
                    entry.getEntryStatus().name(), EntryStatus.APPROVED.name());
        }
        entry.approve();
        log.info("ESG 데이터 승인: entryId={}", entryId);
        return EsgDataEntryResponse.from(entry);
    }

    /** 반려 (SUBMITTED → REJECTED) */
    @Transactional
    public EsgDataEntryResponse rejectDataEntry(Long entryId, String reason) {
        EsgDataEntry entry = findById(entryId);
        if (entry.getEntryStatus() != EntryStatus.SUBMITTED) {
            throw EsgException.invalidStatusTransition(
                    entry.getEntryStatus().name(), EntryStatus.REJECTED.name());
        }
        entry.reject(reason);
        log.info("ESG 데이터 반려: entryId={}, reason={}", entryId, reason);
        return EsgDataEntryResponse.from(entry);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private EsgDataEntry findById(Long entryId) {
        return dataEntryRepository.findById(entryId)
                .orElseThrow(() -> EsgException.dataEntryNotFound(entryId));
    }

    private void validateDraftStatus(EsgDataEntry entry) {
        if (entry.getEntryStatus() != EntryStatus.DRAFT) {
            throw EsgException.invalidStatusTransition(
                    entry.getEntryStatus().name(), "DRAFT");
        }
    }
}
