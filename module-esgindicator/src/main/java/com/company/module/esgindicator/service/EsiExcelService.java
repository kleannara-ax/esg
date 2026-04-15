package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.response.IndicatorValueResponse;
import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.entity.EsiDepartment;
import com.company.module.esgindicator.entity.EsiIndicator;
import com.company.module.esgindicator.entity.EsiIndicatorValue;
import com.company.module.esgindicator.entity.InputStatus;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import com.company.module.esgindicator.repository.EsiIndicatorRepository;
import com.company.module.esgindicator.repository.EsiIndicatorValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * ESG 지표 엑셀 업로드 / 다운로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiExcelService {

    private final EsiIndicatorRepository      indicatorRepository;
    private final EsiDepartmentRepository     deptRepository;
    private final EsiIndicatorValueRepository valueRepository;
    private final EsiIndicatorValueService    valueService;

    // ─────────────────────────────────────────────────────────────────────────
    // 다운로드: Fact Book Export (연도별 전체 데이터)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generateFactBookExcel(Integer year) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle dataStyle    = createDataStyle(workbook);
            CellStyle numberStyle  = createNumberStyle(workbook);

            for (EsgCategory category : EsgCategory.values()) {
                Sheet sheet = workbook.createSheet(category.getKorName() + "(" + category.name() + ")");
                List<EsiDepartment> depts = deptRepository.findAllActive();
                List<EsiIndicator>  indicators = indicatorRepository.findByCategoryActive(category);

                if (indicators.isEmpty()) continue;

                // ── 헤더 행 ────────────────────────────────────────────────
                Row headerRow = sheet.createRow(0);
                createStyledCell(headerRow, 0, "대구분",  headerStyle);
                createStyledCell(headerRow, 1, "중구분",  headerStyle);
                createStyledCell(headerRow, 2, "지표명",  headerStyle);
                createStyledCell(headerRow, 3, "단위",    headerStyle);
                int col = 4;
                for (EsiDepartment dept : depts) {
                    createStyledCell(headerRow, col++, dept.getDeptName(), headerStyle);
                }

                // ── 데이터 행 ──────────────────────────────────────────────
                List<EsiIndicatorValue> values = valueRepository.findByYearAndCategory(year, category);
                Map<Long, Map<Long, EsiIndicatorValue>> map = new LinkedHashMap<>();
                for (EsiIndicatorValue v : values) {
                    map.computeIfAbsent(v.getIndicator().getIndicatorId(), k -> new HashMap<>())
                       .put(v.getDepartment().getDeptId(), v);
                }

                int rowIdx = 1;
                for (EsiIndicator indicator : indicators) {
                    Row row = sheet.createRow(rowIdx++);
                    createStyledCell(row, 0, indicator.getMajorCategory(), dataStyle);
                    createStyledCell(row, 1, indicator.getMidCategory(),   dataStyle);
                    createStyledCell(row, 2, indicator.getMinorCategory(), dataStyle);
                    createStyledCell(row, 3, indicator.getUnit(),          dataStyle);

                    int c = 4;
                    Map<Long, EsiIndicatorValue> deptMap = map.getOrDefault(indicator.getIndicatorId(), Map.of());
                    for (EsiDepartment dept : depts) {
                        EsiIndicatorValue val = deptMap.get(dept.getDeptId());
                        if (val != null && val.getIndicatorValue() != null) {
                            Cell cell = row.createCell(c);
                            cell.setCellValue(val.getIndicatorValue().doubleValue());
                            cell.setCellStyle(numberStyle);
                        } else {
                            createStyledCell(row, c, "-", dataStyle);
                        }
                        c++;
                    }
                }

                // 열 너비 자동조정
                for (int i = 0; i < 4 + depts.size(); i++) sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 다운로드: 입력 템플릿 (부서별 입력용)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generateInputTemplate(Integer year, EsgCategory category, Long deptId) throws IOException {
        EsiDepartment dept = deptRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        List<EsiIndicator> indicators = indicatorRepository.findByCategoryActive(category);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle dataStyle    = createDataStyle(workbook);
            CellStyle inputStyle   = createInputStyle(workbook);
            CellStyle numberStyle  = createNumberStyle(workbook);

            Sheet sheet = workbook.createSheet(category.getKorName() + "_" + dept.getDeptName());

            // 제목 행
            Row titleRow = sheet.createRow(0);
            createStyledCell(titleRow, 0, year + "년 ESG 지표 입력 양식 - " + dept.getDeptName() + " [" + category.getKorName() + "]", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // 헤더 행
            Row headerRow = sheet.createRow(1);
            createStyledCell(headerRow, 0, "지표ID",   headerStyle);
            createStyledCell(headerRow, 1, "대구분",   headerStyle);
            createStyledCell(headerRow, 2, "중구분",   headerStyle);
            createStyledCell(headerRow, 3, "지표명",   headerStyle);
            createStyledCell(headerRow, 4, "단위",     headerStyle);
            createStyledCell(headerRow, 5, "입력값",   headerStyle);
            createStyledCell(headerRow, 6, "산식/비고", headerStyle);

            // 기존 입력 데이터 조회
            Map<Long, EsiIndicatorValue> existingMap = new HashMap<>();
            valueRepository.findByYearAndDept(year, deptId)
                .forEach(v -> existingMap.put(v.getIndicator().getIndicatorId(), v));

            int rowIdx = 2;
            for (EsiIndicator indicator : indicators) {
                Row row = sheet.createRow(rowIdx++);
                createStyledCell(row, 0, String.valueOf(indicator.getIndicatorId()), dataStyle);
                createStyledCell(row, 1, indicator.getMajorCategory(), dataStyle);
                createStyledCell(row, 2, indicator.getMidCategory(),   dataStyle);
                createStyledCell(row, 3, indicator.getMinorCategory(), dataStyle);
                createStyledCell(row, 4, indicator.getUnit(),          dataStyle);

                EsiIndicatorValue existing = existingMap.get(indicator.getIndicatorId());
                if (existing != null && existing.getIndicatorValue() != null) {
                    Cell cell = row.createCell(5);
                    cell.setCellValue(existing.getIndicatorValue().doubleValue());
                    cell.setCellStyle(numberStyle);
                    createStyledCell(row, 6, existing.getFormulaRemark(), inputStyle);
                } else {
                    row.createCell(5).setCellStyle(inputStyle);
                    row.createCell(6).setCellStyle(inputStyle);
                }
            }

            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 업로드: 입력 템플릿 파일로 일괄 저장
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public int uploadInputExcel(Integer year, Long deptId, MultipartFile file) throws IOException {
        EsiDepartment dept = deptRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));

        int savedCount = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // row 2 부터 데이터 (0=제목행, 1=헤더행)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String indicatorIdStr = getCellStringValue(row.getCell(0));
                if (indicatorIdStr == null || indicatorIdStr.isBlank()) continue;

                try {
                    Long indicatorId = Long.parseLong(indicatorIdStr.trim());
                    BigDecimal value = getCellNumericValue(row.getCell(5));
                    String remark    = getCellStringValue(row.getCell(6));

                    Optional<EsiIndicatorValue> optVal =
                        valueRepository.findByIndicatorAndDeptAndYear(indicatorId, deptId, year);

                    EsiIndicatorValue entity;
                    if (optVal.isPresent()) {
                        entity = optVal.get();
                        if (!entity.isConfirmedYn()) {
                            entity.updateValue(value, remark);
                            savedCount++;
                        }
                    } else {
                        EsiIndicator indicator = indicatorRepository.findById(indicatorId).orElse(null);
                        if (indicator == null) continue;
                        entity = EsiIndicatorValue.builder()
                                .indicator(indicator)
                                .department(dept)
                                .baseYear(year)
                                .build();
                        entity = valueRepository.save(entity);
                        entity.updateValue(value, remark);
                        savedCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("[ESI] 엑셀 업로드 파싱 오류: row={}", i);
                }
            }
        }

        log.info("[ESI] 엑셀 업로드 완료: deptId={}, year={}, savedCount={}", deptId, year, savedCount);
        return savedCount;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 스타일 헬퍼
    // ─────────────────────────────────────────────────────────────────────────

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createInputStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNumberStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.######"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };
    }

    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING  -> {
                    String s = cell.getStringCellValue().trim();
                    yield s.isBlank() ? null : new BigDecimal(s);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
