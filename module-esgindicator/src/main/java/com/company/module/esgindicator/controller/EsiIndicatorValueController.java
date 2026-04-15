package com.company.module.esgindicator.controller;

import com.company.common.response.ApiResponse;
import com.company.module.esgindicator.dto.request.ValueBulkSaveRequest;
import com.company.module.esgindicator.dto.request.ValueConfirmRequest;
import com.company.module.esgindicator.dto.request.ValueSaveRequest;
import com.company.module.esgindicator.dto.response.IndicatorSheetResponse;
import com.company.module.esgindicator.dto.response.IndicatorValueResponse;
import com.company.module.esgindicator.dto.response.InputStatusSummaryResponse;
import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.service.EsiExcelService;
import com.company.module.esgindicator.service.EsiFactBookImportService;
import com.company.module.esgindicator.service.EsiIndicatorValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 연도별 ESG 지표값 API
 * URL Prefix: /esgindicator-api/values
 */
@RestController
@RequestMapping("/esgindicator-api/values")
@RequiredArgsConstructor
public class EsiIndicatorValueController {

    private final EsiIndicatorValueService valueService;
    private final EsiExcelService          excelService;
    private final EsiFactBookImportService  factBookImportService;

    // ── 화면 조회 ────────────────────────────────────────────────────────────

    /**
     * 엑셀 시트 형태 데이터 조회 (연도 + ESG구분)
     * GET /esgindicator-api/values/sheet?year=2024&category=E
     */
    @GetMapping("/sheet")
    public ResponseEntity<ApiResponse<IndicatorSheetResponse>> getSheet(
            @RequestParam Integer year,
            @RequestParam EsgCategory category) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.getSheet(year, category)));
    }

    /**
     * 입력 현황 요약
     * GET /esgindicator-api/values/summary?year=2024&category=E
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<InputStatusSummaryResponse>> getSummary(
            @RequestParam Integer year,
            @RequestParam EsgCategory category) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.getInputSummary(year, category)));
    }

    /**
     * 사용 가능한 연도 목록
     * GET /esgindicator-api/values/years
     */
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableYears() {
        return ResponseEntity.ok(ApiResponse.ok(valueService.getAvailableYears()));
    }

    // ── 값 저장 ──────────────────────────────────────────────────────────────

    /**
     * 단건 저장 (UPSERT)
     * POST /esgindicator-api/values
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IndicatorValueResponse>> saveValue(
            @Valid @RequestBody ValueSaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.saveValue(request)));
    }

    /**
     * 일괄 저장 (부서별 화면 저장 버튼)
     * POST /esgindicator-api/values/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<IndicatorValueResponse>>> bulkSave(
            @Valid @RequestBody ValueBulkSaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.bulkSaveValues(request)));
    }

    // ── 상태 전이 ────────────────────────────────────────────────────────────

    /**
     * 제출
     * POST /esgindicator-api/values/{valueId}/submit
     */
    @PostMapping("/{valueId}/submit")
    public ResponseEntity<ApiResponse<IndicatorValueResponse>> submit(@PathVariable Long valueId) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.submitValue(valueId)));
    }

    /**
     * 최종 확정 (관리자)
     * POST /esgindicator-api/values/{valueId}/confirm
     */
    @PostMapping("/{valueId}/confirm")
    public ResponseEntity<ApiResponse<IndicatorValueResponse>> confirm(
            @PathVariable Long valueId,
            @RequestBody(required = false) ValueConfirmRequest request) {
        if (request == null) request = new ValueConfirmRequest();
        return ResponseEntity.ok(ApiResponse.ok(valueService.confirmValue(valueId, request)));
    }

    /**
     * 확정 취소 (관리자)
     * POST /esgindicator-api/values/{valueId}/cancel-confirm
     */
    @PostMapping("/{valueId}/cancel-confirm")
    public ResponseEntity<ApiResponse<IndicatorValueResponse>> cancelConfirm(
            @PathVariable Long valueId) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.cancelConfirmValue(valueId)));
    }

    // ── 엑셀 ─────────────────────────────────────────────────────────────────

    /**
     * Fact Book Excel 다운로드
     * GET /esgindicator-api/values/export/factbook?year=2024
     */
    @GetMapping("/export/factbook")
    public ResponseEntity<byte[]> downloadFactBook(@RequestParam Integer year) throws IOException {
        byte[] excel = excelService.generateFactBookExcel(year);
        String filename = URLEncoder.encode("ESG_FactBook_" + year + ".xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(excel);
    }

    /**
     * 입력 템플릿 다운로드 (부서별)
     * GET /esgindicator-api/values/export/template?year=2024&category=E&deptId=1
     */
    @GetMapping("/export/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestParam Integer year,
            @RequestParam EsgCategory category,
            @RequestParam Long deptId) throws IOException {
        byte[] excel = excelService.generateInputTemplate(year, category, deptId);
        String filename = URLEncoder.encode("ESG_입력양식_" + year + "_" + category.getKorName() + ".xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(excel);
    }

    /**
     * 입력 템플릿 업로드 (일괄 입력)
     * POST /esgindicator-api/values/upload?year=2024&deptId=1
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadExcel(
            @RequestParam Integer year,
            @RequestParam Long deptId,
            @RequestParam MultipartFile file) throws IOException {
        int count = excelService.uploadInputExcel(year, deptId, file);
        return ResponseEntity.ok(ApiResponse.ok(count + "건이 저장되었습니다.", null));
    }

    /**
     * Fact Book 조회 데이터 (API용, 화면 없이 JSON)
     * GET /esgindicator-api/values/export/data?year=2024
     */
    @GetMapping("/export/data")
    public ResponseEntity<ApiResponse<List<IndicatorValueResponse>>> getExportData(
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(valueService.getExportData(year)));
    }

    /**
     * ESG Fact Book 실제 엑셀 파일 일괄 임포트 (지표 마스터 + 연도별 값)
     * POST /esgindicator-api/values/import/factbook
     * - Environment(환경), Social(사회), Governance(지배구조) 시트 자동 파싱
     * - 부서/지표 신규 생성 또는 기존 매핑
     */
    @PostMapping("/import/factbook")
    public ResponseEntity<ApiResponse<String>> importFactBook(
            @RequestParam MultipartFile file) throws IOException {
        EsiFactBookImportService.ImportResult result = factBookImportService.importFactBook(file);
        return ResponseEntity.ok(ApiResponse.ok(result.toString(), null));
    }
}
