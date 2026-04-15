package com.company.module.esgindicator.repository;

import com.company.module.esgindicator.entity.EsiDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 부서 마스터 Repository
 */
public interface EsiDepartmentRepository extends JpaRepository<EsiDepartment, Long> {

    /** 사용중인 부서 목록 (정렬순서 오름차순) */
    @Query("SELECT d FROM EsiDepartment d WHERE d.useYn = true ORDER BY d.sortOrder, d.deptName")
    List<EsiDepartment> findAllActive();

    /** 부서 코드로 조회 */
    Optional<EsiDepartment> findByDeptCode(String deptCode);

    /** 부서 코드 중복 확인 */
    boolean existsByDeptCode(String deptCode);

    /** 부서 코드 중복 확인 (자기 자신 제외) */
    boolean existsByDeptCodeAndDeptIdNot(String deptCode, Long deptId);
}
