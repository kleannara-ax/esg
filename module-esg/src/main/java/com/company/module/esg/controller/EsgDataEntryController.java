package com.company.module.esg.controller;

import com.company.common.response.ApiResponse;
import com.company.module.esg.dto.request.EsgDataEntryRequest;
import com.company.module.esg.dto.response.EsgDataEntryResponse;
import com.company.module.esg.entity.EntryStatus;
import com.company.module.esg.service.EsgDataEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ESG 데이터 입력 Controller
 * <p>URL Prefix: /esg-api/data-entries</p>
 *
 * <pre>
 * GET    /esg-api/data-entries/monthly?year=2024&month=1              → 월별 조회
 * GET    /esg-api/data-entries/annual?indicatorId=1&year=2024         → 연간 추이
 * GET    /esg-api/data-entries/{id}                                   → 단건 조회
 * POST   /esg-api/data-entries                                        → 데이터 입력
 * PUT    /esg-api/data-entries/{id}                                   → 데이터 수정
 * PATCH  /esg-api/data-entries/{id}/submit                           → 제출
 * PATCH  /esg-api/data-entries/{id}/approve                          → 승인 (MANAGER+)
 * PATCH  /esg-api/data-entries/{id}/reject                           → 반려 (MANAGER+)
 * </pre>
 */
@RestController
@RequestMapping("/esg-api/data-entries")
@RequiredArgsConstructor
public class EsgDataEntryController {

    private final EsgDataEntryService dataEntryService;

    /**
     * 월별 ESG 데이터 조회
     * GET /esg-api/data-entries/monthly?year=2024&month=1&status=APPROVED
     */
    @GetMapping("/monthly")
    public ApiResponse<List<EsgDataEntryResponse>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) EntryStatus status) {
        return ApiResponse.ok(dataEntryService.getMonthlyReport(year, month, status));
    }

    /**
     * 지표별 연간 추이 조회
     * GET /esg-api/data-entries/annual?indicatorId=1&year=2024
     */
    @GetMapping("/annual")
    public ApiResponse<List<EsgDataEntryResponse>> getAnnualTrend(
            @RequestParam Long indicatorId,
            @RequestParam int year) {
        return ApiResponse.ok(dataEntryService.getAnnualTrend(indicatorId, year));
    }

    /**
     * 데이터 단건 조회
     * GET /esg-api/data-entries/{entryId}
     */
    @GetMapping("/{entryId}")
    public ApiResponse<EsgDataEntryResponse> getDataEntry(@PathVariable Long entryId) {
        return ApiResponse.ok(dataEntryService.getDataEntry(entryId));
    }

    /**
     * ESG 데이터 입력
     * POST /esg-api/data-entries
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EsgDataEntryResponse> createDataEntry(
            @Valid @RequestBody EsgDataEntryRequest request) {
        return ApiResponse.created(dataEntryService.createDataEntry(request));
    }

    /**
     * ESG 데이터 수정 (DRAFT 상태만 가능)
     * PUT /esg-api/data-entries/{entryId}
     */
    @PutMapping("/{entryId}")
    public ApiResponse<EsgDataEntryResponse> updateDataEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody EsgDataEntryRequest request) {
        return ApiResponse.ok(dataEntryService.updateDataEntry(entryId, request));
    }

    /**
     * 데이터 제출 (DRAFT → SUBMITTED)
     * PATCH /esg-api/data-entries/{entryId}/submit
     */
    @PatchMapping("/{entryId}/submit")
    public ApiResponse<EsgDataEntryResponse> submitDataEntry(@PathVariable Long entryId) {
        return ApiResponse.ok(dataEntryService.submitDataEntry(entryId));
    }

    /**
     * 데이터 승인 (SUBMITTED → APPROVED)
     * PATCH /esg-api/data-entries/{entryId}/approve
     */
    @PatchMapping("/{entryId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<EsgDataEntryResponse> approveDataEntry(@PathVariable Long entryId) {
        return ApiResponse.ok(dataEntryService.approveDataEntry(entryId));
    }

    /**
     * 데이터 반려 (SUBMITTED → REJECTED)
     * PATCH /esg-api/data-entries/{entryId}/reject
     */
    @PatchMapping("/{entryId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<EsgDataEntryResponse> rejectDataEntry(
            @PathVariable Long entryId,
            @RequestParam String reason) {
        return ApiResponse.ok(dataEntryService.rejectDataEntry(entryId, reason));
    }
}
