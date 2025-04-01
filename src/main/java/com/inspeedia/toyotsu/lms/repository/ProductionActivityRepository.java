package com.inspeedia.toyotsu.lms.repository;

import com.inspeedia.toyotsu.lms.model.ProductionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionActivityRepository extends JpaRepository<ProductionActivity, String> {
    List<ProductionActivity> findProductionActivityByDepartmentAndLine(String department, String line);


    List<ProductionActivity> findProductionActivityByDate(LocalDate localDate);

    @Query("SELECT pa FROM ProductionActivity pa WHERE pa.date BETWEEN :startDate AND :endDate")
    List<ProductionActivity> findProductionActivityByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<ProductionActivity> findProductionActivityByDepartment(String dept);

    List<ProductionActivity> findProductionActivityByDepartmentAndDate(String dept, LocalDate startDate);

    List<ProductionActivity> findProductionActivityByDepartmentAndLineAndDate(String department, String line, LocalDate date);

}