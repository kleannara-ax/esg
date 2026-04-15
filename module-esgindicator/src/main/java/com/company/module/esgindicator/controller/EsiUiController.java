package com.company.module.esgindicator.controller;

import com.company.module.esgindicator.dto.response.IndicatorSheetResponse;
import com.company.module.esgindicator.dto.response.InputStatusSummaryResponse;
import com.company.module.esgindicator.entity.EsgCategory;
import com.company.module.esgindicator.service.EsiDepartmentService;
import com.company.module.esgindicator.service.EsiIndicatorService;
import com.company.module.esgindicator.service.EsiIndicatorValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;
import java.util.List;

/**
 * ESG 지표 관리 UI 컨트롤러 (Thymeleaf 화면)
 * URL Prefix: /esgindicator-api/ui/**
 */
@Controller
@RequestMapping("/esgindicator-api/ui")
@RequiredArgsConstructor
public class EsiUiController {

    private final EsiIndicatorValueService valueService;
    private final EsiDepartmentService     deptService;
    private final EsiIndicatorService      indicatorService;

    /**
     * 메인 지표 입력 화면
     * GET /esgindicator-api/ui?year=2024&category=E&deptId=1(optional)
     */
    @GetMapping
    public String mainSheet(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().value}") Integer year,
            @RequestParam(defaultValue = "E") EsgCategory category,
            @RequestParam(required = false) Long deptId,
            Model model) {

        // 연도 목록 (없으면 현재 연도 포함)
        List<Integer> years = valueService.getAvailableYears();
        if (years.isEmpty()) {
            years = List.of(Year.now().getValue());
        }

        IndicatorSheetResponse sheet = valueService.getSheet(year, category);
        InputStatusSummaryResponse summary = valueService.getInputSummary(year, category);

        model.addAttribute("year", year);
        model.addAttribute("years", years);
        model.addAttribute("category", category.name());
        model.addAttribute("categoryName", category.getKorName());
        model.addAttribute("sheet", sheet);
        model.addAttribute("summary", summary);
        model.addAttribute("selectedDeptId", deptId);

        return "esgindicator/layout";
    }

    /**
     * 부서 관리 화면
     * GET /esgindicator-api/ui/departments
     */
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", deptService.getAllDepartments());
        model.addAttribute("year", Year.now().getValue());
        model.addAttribute("category", "E");
        model.addAttribute("categoryName", "");
        model.addAttribute("sheet", null);
        model.addAttribute("summary", null);
        model.addAttribute("selectedDeptId", null);
        return "esgindicator/layout";
    }

    /**
     * 지표 마스터 관리 화면
     * GET /esgindicator-api/ui/indicators?category=E
     */
    @GetMapping("/indicators")
    public String indicators(
            @RequestParam(defaultValue = "E") EsgCategory category,
            Model model) {
        model.addAttribute("indicators", indicatorService.getIndicatorsByCategory(category));
        model.addAttribute("year", Year.now().getValue());
        model.addAttribute("category", category.name());
        model.addAttribute("categoryName", category.getKorName());
        model.addAttribute("sheet", null);
        model.addAttribute("summary", null);
        model.addAttribute("selectedDeptId", null);
        return "esgindicator/layout";
    }
}
