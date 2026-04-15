package com.company.module.esgindicator.controller;

import com.company.common.response.ApiResponse;
import com.company.module.esgindicator.dto.request.IndicatorCreateRequest;
import com.company.module.esgindicator.dto.response.IndicatorResponse;
import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.service.EsiIndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ESG 지표 마스터 API
 * URL Prefix: /esgindicator-api/indicators
 */
@RestController
@RequestMapping("/esgindicator-api/indicators")
@RequiredArgsConstructor
public class EsiIndicatorController {

    private final EsiIndicatorService indicatorService;

    /**
     * 카테고리별 지표 목록 조회
     * GET /esgindicator-api/indicators?category=E
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<IndicatorResponse>>> getIndicators(
            @RequestParam(required = false) EsgCategory category) {
        List<IndicatorResponse> result = (category != null)
                ? indicatorService.getIndicatorsByCategory(category)
                : indicatorService.getAllIndicators();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 지표 단건 조회
     * GET /esgindicator-api/indicators/{indicatorId}
     */
    @GetMapping("/{indicatorId}")
    public ResponseEntity<ApiResponse<IndicatorResponse>> getIndicator(
            @PathVariable Long indicatorId) {
        return ResponseEntity.ok(ApiResponse.ok(indicatorService.getIndicator(indicatorId)));
    }

    /**
     * 지표 등록 (관리자)
     * POST /esgindicator-api/indicators
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IndicatorResponse>> createIndicator(
            @Valid @RequestBody IndicatorCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.created(indicatorService.createIndicator(request)));
    }

    /**
     * 지표 수정 (관리자)
     * PUT /esgindicator-api/indicators/{indicatorId}
     */
    @PutMapping("/{indicatorId}")
    public ResponseEntity<ApiResponse<IndicatorResponse>> updateIndicator(
            @PathVariable Long indicatorId,
            @Valid @RequestBody IndicatorCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(indicatorService.updateIndicator(indicatorId, request)));
    }
}
