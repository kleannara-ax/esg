package com.company.module.esgindicator.controller;

import com.company.common.response.ApiResponse;
import com.company.module.esgindicator.dto.request.DepartmentCreateRequest;
import com.company.module.esgindicator.dto.response.DepartmentResponse;
import com.company.module.esgindicator.service.EsiDepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 부서 마스터 API
 * URL Prefix: /esgindicator-api/departments
 */
@RestController
@RequestMapping("/esgindicator-api/departments")
@RequiredArgsConstructor
public class EsiDepartmentController {

    private final EsiDepartmentService deptService;

    /**
     * 활성 부서 목록 조회
     * GET /esgindicator-api/departments/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getActiveDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(deptService.getActiveDepartments()));
    }

    /**
     * 전체 부서 목록 조회 (관리자)
     * GET /esgindicator-api/departments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(deptService.getAllDepartments()));
    }

    /**
     * 부서 단건 조회
     * GET /esgindicator-api/departments/{deptId}
     */
    @GetMapping("/{deptId}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(ApiResponse.ok(deptService.getDepartment(deptId)));
    }

    /**
     * 부서 등록 (관리자)
     * POST /esgindicator-api/departments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.created(deptService.createDepartment(request)));
    }

    /**
     * 부서 수정 (관리자)
     * PUT /esgindicator-api/departments/{deptId}
     */
    @PutMapping("/{deptId}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long deptId,
            @Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(deptService.updateDepartment(deptId, request)));
    }
}
