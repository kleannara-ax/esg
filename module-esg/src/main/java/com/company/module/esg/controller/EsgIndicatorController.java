package com.company.module.esg.controller;

import com.company.common.response.ApiResponse;
import com.company.module.esg.dto.request.EsgIndicatorCreateRequest;
import com.company.module.esg.dto.request.EsgIndicatorUpdateRequest;
import com.company.module.esg.dto.response.EsgIndicatorResponse;
import com.company.module.esg.entity.EsgCategory;
import com.company.module.esg.service.EsgIndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ESG 지표 관리 Controller
 * <p>URL Prefix: /esg-api/indicators</p>
 *
 * <pre>
 * GET    /esg-api/indicators               → 활성 지표 전체 조회
 * GET    /esg-api/indicators?category=E    → 카테고리별 조회
 * GET    /esg-api/indicators/{id}          → 단건 조회
 * POST   /esg-api/indicators               → 지표 등록 (ADMIN/MANAGER)
 * PUT    /esg-api/indicators/{id}          → 지표 수정 (ADMIN/MANAGER)
 * PATCH  /esg-api/indicators/{id}/deactivate → 비활성화 (ADMIN)
 * PATCH  /esg-api/indicators/{id}/activate   → 활성화   (ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/esg-api/indicators")
@RequiredArgsConstructor
public class EsgIndicatorController {

    private final EsgIndicatorService indicatorService;

    /**
     * 활성 지표 전체 또는 카테고리별 조회
     * GET /esg-api/indicators
     * GET /esg-api/indicators?category=E
     */
    @GetMapping
    public ApiResponse<List<EsgIndicatorResponse>> getIndicators(
            @RequestParam(required = false) EsgCategory category) {
        List<EsgIndicatorResponse> result = (category != null)
                ? indicatorService.getIndicatorsByCategory(category)
                : indicatorService.getActiveIndicators();
        return ApiResponse.ok(result);
    }

    /**
     * 지표 단건 조회
     * GET /esg-api/indicators/{indicatorId}
     */
    @GetMapping("/{indicatorId}")
    public ApiResponse<EsgIndicatorResponse> getIndicator(@PathVariable Long indicatorId) {
        return ApiResponse.ok(indicatorService.getIndicator(indicatorId));
    }

    /**
     * 지표 등록
     * POST /esg-api/indicators
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<EsgIndicatorResponse> createIndicator(
            @Valid @RequestBody EsgIndicatorCreateRequest request) {
        return ApiResponse.created(indicatorService.createIndicator(request));
    }

    /**
     * 지표 수정
     * PUT /esg-api/indicators/{indicatorId}
     */
    @PutMapping("/{indicatorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<EsgIndicatorResponse> updateIndicator(
            @PathVariable Long indicatorId,
            @Valid @RequestBody EsgIndicatorUpdateRequest request) {
        return ApiResponse.ok(indicatorService.updateIndicator(indicatorId, request));
    }

    /**
     * 지표 비활성화
     * PATCH /esg-api/indicators/{indicatorId}/deactivate
     */
    @PatchMapping("/{indicatorId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateIndicator(@PathVariable Long indicatorId) {
        indicatorService.deactivateIndicator(indicatorId);
        return ApiResponse.ok("지표가 비활성화되었습니다.", null);
    }

    /**
     * 지표 활성화
     * PATCH /esg-api/indicators/{indicatorId}/activate
     */
    @PatchMapping("/{indicatorId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateIndicator(@PathVariable Long indicatorId) {
        indicatorService.activateIndicator(indicatorId);
        return ApiResponse.ok("지표가 활성화되었습니다.", null);
    }
}
