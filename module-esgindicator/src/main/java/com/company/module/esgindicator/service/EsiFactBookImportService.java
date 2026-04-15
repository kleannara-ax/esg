package com.company.module.esgindicator.service;

import com.company.module.esgindicator.entity.*;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import com.company.module.esgindicator.repository.EsiIndicatorRepository;
import com.company.module.esgindicator.repository.EsiIndicatorValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * ESG Fact Book 실제 엑셀 파일 임포트 서비스
 *
 * 엑셀 형식:
 *  - 시트: Environment(환경), Social(사회), Governance(지배구조)
 *  - Environment 헤더: Row 7, 연도컬럼: 2020(G)~2025(L)
 *  - Social/Governance 헤더: Row 6, 연도컬럼: 2020(G),2021(H),2022(I),2023(R),2024(T)
 *  - 컬럼구조: 담당부서(A), 제목(B), 표대제목(C), 표중제목(D), 표소제목(E), 단위(F), 연도...
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiFactBookImportService {

    private final EsiIndicatorRepository      indicatorRepository;
    private final EsiDepartmentRepository     deptRepository;
    private final EsiIndicatorValueRepository valueRepository;

    // ── 전체 임포트 결과 ────────────────────────────────────────────────────
    public static class ImportResult {
        public int deptCreated   = 0;
        public int indicatorCreated = 0;
        public int valueUpserted  = 0;
        public int errorCount     = 0;
        public List<String> errors = new ArrayList<>();

        @Override
        public String toString() {
            return String.format("부서 %d개 생성, 지표 %d개 생성, 값 %d개 저장, 오류 %d건",
                    deptCreated, indicatorCreated, valueUpserted, errorCount);
        }
    }

    // ── 임포트 진입점 ──────────────────────────────────────────────────────
    @Transactional
    public ImportResult importFactBook(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            // 시트별 임포트
            importSheet(wb, "Environment(환경)", EsgCategory.E,
                    7,  // 1-based header row
                    new int[]{6, 7, 8, 9, 10, 11},  // 0-based year columns (2020-2025)
                    new int[]{2020, 2021, 2022, 2023, 2024, 2025},
                    result);

            importSheet(wb, "Social(사회)", EsgCategory.S,
                    6,
                    new int[]{6, 7, 8, 17, 19},
                    new int[]{2020, 2021, 2022, 2023, 2024},
                    result);

            importSheet(wb, "Governance(지배구조)", EsgCategory.G,
                    6,
                    new int[]{6, 7, 8, 17, 18},
                    new int[]{2020, 2021, 2022, 2023, 2024},
                    result);
        }

        log.info("[ESI] Fact Book 임포트 완료: {}", result);
        return result;
    }

    // ── 시트별 처리 ────────────────────────────────────────────────────────
    private void importSheet(Workbook wb, String sheetName, EsgCategory category,
                              int headerRowNum, int[] yearColIdxs, int[] years,
                              ImportResult result) {
        Sheet sheet = wb.getSheet(sheetName);
        if (sheet == null) {
            log.warn("[ESI] 시트 없음: {}", sheetName);
            result.errors.add("시트 없음: " + sheetName);
            return;
        }

        // 부서 캐시 (이름 → entity)
        Map<String, EsiDepartment> deptCache = new LinkedHashMap<>();
        for (EsiDepartment d : deptRepository.findAll()) deptCache.put(d.getDeptName(), d);

        // 지표 캐시 (category+minor → entity)
        Map<String, EsiIndicator> indicatorCache = new LinkedHashMap<>();
        for (EsiIndicator ind : indicatorRepository.findAll()) {
            indicatorCache.put(category.name() + "::" + ind.getMinorCategory(), ind);
        }

        // 이전 행 값 carry-forward
        String prevDept = "", prevTitle = "", prevMajor = "", prevMid = "";
        int sortOrder = indicatorRepository.findByCategoryActive(category).size();

        for (int ri = headerRowNum; ri <= sheet.getLastRowNum(); ri++) {
            Row row = sheet.getRow(ri);
            if (row == null) continue;

            // ── 컬럼 파싱 ────────────────────────────────────────────
            String dept  = strVal(row.getCell(0));
            String title = strVal(row.getCell(1));
            String major = strVal(row.getCell(2));
            String mid   = strVal(row.getCell(3));
            String minor = strVal(row.getCell(4));
            String unit  = strVal(row.getCell(5));

            // carry-forward
            if (!isBlank(dept))  prevDept  = dept;  else dept  = prevDept;
            if (!isBlank(title)) prevTitle = title;  else title = prevTitle;
            if (!isBlank(major)) prevMajor = major;  else major = prevMajor;
            if (!isBlank(mid))   prevMid   = mid;    else mid   = prevMid;

            if (isBlank(minor)) continue;

            // ── 부서 조회/생성 ────────────────────────────────────────
            EsiDepartment deptEntity = deptCache.get(dept);
            if (deptEntity == null && !isBlank(dept)) {
                int nextOrder = deptCache.size() + 1;
                String code   = "DEPT" + String.format("%03d", nextOrder);
                // check duplicate code
                while (deptRepository.existsByDeptCode(code)) {
                    nextOrder++;
                    code = "DEPT" + String.format("%03d", nextOrder);
                }
                deptEntity = EsiDepartment.builder()
                        .deptCode(code)
                        .deptName(dept)
                        .useYn(true)
                        .sortOrder(nextOrder)
                        .build();
                deptEntity = deptRepository.save(deptEntity);
                deptCache.put(dept, deptEntity);
                result.deptCreated++;
                log.debug("[ESI] 부서 생성: {}", dept);
            }

            // ── 지표 조회/생성 ─────────────────────────────────────────
            String indicatorKey = category.name() + "::" + minor;
            EsiIndicator indicatorEntity = indicatorCache.get(indicatorKey);
            if (indicatorEntity == null) {
                sortOrder++;
                indicatorEntity = EsiIndicator.builder()
                        .esgCategory(category)
                        .department(deptEntity)
                        .title(title)
                        .majorCategory(major)
                        .midCategory(mid)
                        .minorCategory(minor)
                        .unit(unit)
                        .sortOrder(sortOrder)
                        .useYn(true)
                        .build();
                indicatorEntity = indicatorRepository.save(indicatorEntity);
                indicatorCache.put(indicatorKey, indicatorEntity);
                result.indicatorCreated++;
                log.debug("[ESI] 지표 생성: {} > {}", major, minor);
            }

            // ── 연도별 값 UPSERT ──────────────────────────────────────
            for (int yi = 0; yi < yearColIdxs.length; yi++) {
                int year = years[yi];
                int colIdx = yearColIdxs[yi];

                Cell cell = row.getCell(colIdx);
                BigDecimal numVal = numericVal(cell);
                String rawStr = strVal(cell);

                // 메모/비고 텍스트 (혼합형 값 처리)
                String remark = null;
                if (!isBlank(rawStr) && numVal == null) {
                    remark = rawStr.length() > 500 ? rawStr.substring(0, 500) : rawStr;
                } else if (!isBlank(rawStr) && !rawStr.trim().matches("[\\d.\\-+eE]+")) {
                    remark = rawStr.length() > 500 ? rawStr.substring(0, 500) : rawStr;
                }

                try {
                    Optional<EsiIndicatorValue> existing = valueRepository.findByIndicatorAndDeptAndYear(
                            indicatorEntity.getIndicatorId(),
                            deptEntity.getDeptId(),
                            year);

                    if (existing.isPresent()) {
                        EsiIndicatorValue v = existing.get();
                        if (!v.isConfirmedYn()) {
                            v.updateValue(numVal, remark);
                            result.valueUpserted++;
                        }
                    } else {
                        EsiIndicatorValue v = EsiIndicatorValue.builder()
                                .indicator(indicatorEntity)
                                .department(deptEntity)
                                .baseYear(year)
                                .build();
                        v = valueRepository.save(v);
                        v.updateValue(numVal, remark);
                        if (numVal != null || remark != null) {
                            result.valueUpserted++;
                        }
                    }
                } catch (Exception e) {
                    result.errorCount++;
                    String msg = String.format("오류 [%s/%d행/%d년]: %s", sheetName, ri + 1, year, e.getMessage());
                    result.errors.add(msg);
                    log.warn("[ESI] {}", msg);
                }
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String strVal(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue().trim(); }
            }
            default -> "";
        };
    }

    private BigDecimal numericVal(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING  -> {
                    String s = cell.getStringCellValue().trim().replace(",", "");
                    // 숫자로 시작하는 혼합값 (예: "96(15.8)")에서 앞 숫자 추출
                    java.util.regex.Matcher m =
                        java.util.regex.Pattern.compile("^[+-]?[\\d.]+").matcher(s);
                    yield (m.find() && !m.group().isBlank()) ? new BigDecimal(m.group()) : null;
                }
                case FORMULA -> BigDecimal.valueOf(cell.getNumericCellValue());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
