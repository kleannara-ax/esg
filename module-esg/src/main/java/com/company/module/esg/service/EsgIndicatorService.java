package com.company.module.esg.service;

import com.company.module.esg.dto.request.EsgIndicatorCreateRequest;
import com.company.module.esg.dto.request.EsgIndicatorUpdateRequest;
import com.company.module.esg.dto.response.EsgIndicatorResponse;
import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.entity.EsgIndicator;
import com.company.module.esg.exception.EsgException;
import com.company.module.esg.repository.EsgIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ESG 지표 Service
 * <p>@Transactional 은 Service 계층에서만 사용</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsgIndicatorService {

    private final EsgIndicatorRepository indicatorRepository;

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** 전체 활성 지표 목록 조회 */
    @Transactional(readOnly = true)
    public List<EsgIndicatorResponse> getActiveIndicators() {
        return indicatorRepository.findAllByActiveYnTrue()
                .stream()
                .map(EsgIndicatorResponse::from)
                .toList();
    }

    /** 카테고리별 지표 조회 */
    @Transactional(readOnly = true)
    public List<EsgIndicatorResponse> getIndicatorsByCategory(EsgCategory category) {
        return indicatorRepository.findByOptionalCategory(category)
                .stream()
                .map(EsgIndicatorResponse::from)
                .toList();
    }

    /** 지표 단건 조회 */
    @Transactional(readOnly = true)
    public EsgIndicatorResponse getIndicator(Long indicatorId) {
        return EsgIndicatorResponse.from(findById(indicatorId));
    }

    // ── 생성 ────────────────────────────────────────────────────────────────

    /** ESG 지표 등록 */
    @Transactional
    public EsgIndicatorResponse createIndicator(EsgIndicatorCreateRequest request) {
        validateDuplicateCode(request.getIndicatorCode());

        EsgIndicator indicator = EsgIndicator.builder()
                .category(request.getCategory())
                .indicatorCode(request.getIndicatorCode())
                .indicatorName(request.getIndicatorName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .activeYn(true)
                .build();

        EsgIndicator saved = indicatorRepository.save(indicator);
        log.info("ESG 지표 등록: code={}, name={}", saved.getIndicatorCode(), saved.getIndicatorName());
        return EsgIndicatorResponse.from(saved);
    }

    // ── 수정 ────────────────────────────────────────────────────────────────

    /** ESG 지표 수정 */
    @Transactional
    public EsgIndicatorResponse updateIndicator(Long indicatorId, EsgIndicatorUpdateRequest request) {
        EsgIndicator indicator = findById(indicatorId);
        indicator.update(request.getIndicatorName(), request.getDescription(), request.getUnit());
        log.info("ESG 지표 수정: id={}, name={}", indicatorId, request.getIndicatorName());
        return EsgIndicatorResponse.from(indicator);
    }

    /** ESG 지표 비활성화 */
    @Transactional
    public void deactivateIndicator(Long indicatorId) {
        EsgIndicator indicator = findById(indicatorId);
        indicator.deactivate();
        log.info("ESG 지표 비활성화: id={}", indicatorId);
    }

    /** ESG 지표 활성화 */
    @Transactional
    public void activateIndicator(Long indicatorId) {
        EsgIndicator indicator = findById(indicatorId);
        indicator.activate();
        log.info("ESG 지표 활성화: id={}", indicatorId);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private EsgIndicator findById(Long indicatorId) {
        return indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> EsgException.indicatorNotFound(indicatorId));
    }

    private void validateDuplicateCode(String code) {
        if (indicatorRepository.existsByIndicatorCode(code)) {
            throw EsgException.indicatorCodeDuplicated(code);
        }
    }
}
