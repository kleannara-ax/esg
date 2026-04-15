package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.request.IndicatorCreateRequest;
import com.company.module.esgindicator.dto.response.IndicatorResponse;
import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.entity.EsiDepartment;
import com.company.module.esgindicator.entity.EsiIndicator;
import com.company.module.esgindicator.exception.EsiException;
import com.company.module.esgindicator.repository.EsiIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ESG 지표 마스터 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiIndicatorService {

    private final EsiIndicatorRepository indicatorRepository;
    private final EsiDepartmentService   deptService;

    // ── 조회 ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IndicatorResponse> getIndicatorsByCategory(EsgCategory category) {
        return indicatorRepository.findByCategoryActive(category)
                .stream()
                .map(IndicatorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IndicatorResponse> getAllIndicators() {
        return indicatorRepository.findAllActive()
                .stream()
                .map(IndicatorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IndicatorResponse getIndicator(Long indicatorId) {
        return IndicatorResponse.from(findById(indicatorId));
    }

    // ── 등록 ─────────────────────────────────────────────────────────────────

    @Transactional
    public IndicatorResponse createIndicator(IndicatorCreateRequest request) {
        EsiDepartment dept = (request.getDeptId() != null)
                ? deptService.findDeptById(request.getDeptId())
                : null;

        EsiIndicator indicator = EsiIndicator.builder()
                .esgCategory(request.getEsgCategory())
                .department(dept)
                .title(request.getTitle())
                .majorCategory(request.getMajorCategory())
                .midCategory(request.getMidCategory())
                .minorCategory(request.getMinorCategory())
                .unit(request.getUnit())
                .excelRowNum(request.getExcelRowNum())
                .sortOrder(request.getSortOrder())
                .useYn(request.isUseYn())
                .remark(request.getRemark())
                .build();

        EsiIndicator saved = indicatorRepository.save(indicator);
        log.info("[ESI] 지표 등록: category={}, minorCategory={}", saved.getEsgCategory(), saved.getMinorCategory());
        return IndicatorResponse.from(saved);
    }

    // ── 수정 ─────────────────────────────────────────────────────────────────

    @Transactional
    public IndicatorResponse updateIndicator(Long indicatorId, IndicatorCreateRequest request) {
        EsiIndicator indicator = findById(indicatorId);
        EsiDepartment dept = (request.getDeptId() != null)
                ? deptService.findDeptById(request.getDeptId())
                : null;

        indicator.update(
                request.getTitle(),
                request.getMajorCategory(),
                request.getMidCategory(),
                request.getMinorCategory(),
                request.getUnit(),
                request.getRemark(),
                request.getSortOrder(),
                request.isUseYn()
        );
        indicator.changeDepartment(dept);

        log.info("[ESI] 지표 수정: indicatorId={}", indicatorId);
        return IndicatorResponse.from(indicator);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    public EsiIndicator findById(Long indicatorId) {
        return indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> EsiException.indicatorNotFound(indicatorId));
    }
}
