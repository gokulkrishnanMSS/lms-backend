package com.inspeedia.toyotsu.lms.repository;

import com.inspeedia.toyotsu.lms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    List<Employee> findByDepartmentAndIsActiveTrue(String department);
}
