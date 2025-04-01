package com.inspeedia.toyotsu.lms.repository;

import com.inspeedia.toyotsu.lms.model.LineConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface LineConfigRepository extends JpaRepository<LineConfig, Long> {
    List<LineConfig> findByDepartment(String departmentName);
    List<LineConfig> findByDepartmentAndDate(String departmentName, LocalDate date);
    @Transactional
    void deleteByDepartmentAndDate(String department, LocalDate date);
}
