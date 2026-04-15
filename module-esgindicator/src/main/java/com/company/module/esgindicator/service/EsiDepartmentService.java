package com.company.module.esgindicator.service;

import com.company.module.esgindicator.dto.request.DepartmentCreateRequest;
import com.company.module.esgindicator.dto.response.DepartmentResponse;
import com.company.module.esgindicator.entity.EsiDepartment;
import com.company.module.esgindicator.exception.EsiException;
import com.company.module.esgindicator.repository.EsiDepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부서 마스터 서비스
 * @Transactional 은 서비스 레이어에서만 사용 (요구사항 준수)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsiDepartmentService {

    private final EsiDepartmentRepository deptRepository;

    // ── 조회 ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveDepartments() {
        return deptRepository.findAllActive()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return deptRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(Long deptId) {
        return DepartmentResponse.from(findDeptById(deptId));
    }

    // ── 등록 ─────────────────────────────────────────────────────────────────

    @Transactional
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        // 부서코드 중복 검증
        if (deptRepository.existsByDeptCode(request.getDeptCode())) {
            throw EsiException.duplicateDeptCode(request.getDeptCode());
        }

        EsiDepartment dept = EsiDepartment.builder()
                .deptCode(request.getDeptCode())
                .deptName(request.getDeptName())
                .useYn(request.isUseYn())
                .sortOrder(request.getSortOrder())
                .build();

        EsiDepartment saved = deptRepository.save(dept);
        log.info("[ESI] 부서 등록: deptCode={}, deptName={}", saved.getDeptCode(), saved.getDeptName());
        return DepartmentResponse.from(saved);
    }

    // ── 수정 ─────────────────────────────────────────────────────────────────

    @Transactional
    public DepartmentResponse updateDepartment(Long deptId, DepartmentCreateRequest request) {
        EsiDepartment dept = findDeptById(deptId);

        // 부서코드 중복 검증 (자기 자신 제외)
        if (deptRepository.existsByDeptCodeAndDeptIdNot(request.getDeptCode(), deptId)) {
            throw EsiException.duplicateDeptCode(request.getDeptCode());
        }

        dept.update(request.getDeptName(), request.isUseYn(), request.getSortOrder());
        log.info("[ESI] 부서 수정: deptId={}, deptName={}", deptId, dept.getDeptName());
        return DepartmentResponse.from(dept);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    public EsiDepartment findDeptById(Long deptId) {
        return deptRepository.findById(deptId)
                .orElseThrow(() -> EsiException.departmentNotFound(deptId));
    }
}
