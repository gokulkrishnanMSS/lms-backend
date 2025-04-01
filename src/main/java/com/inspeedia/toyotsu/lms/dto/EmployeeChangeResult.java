package com.inspeedia.toyotsu.lms.dto;

import com.inspeedia.toyotsu.lms.enums.EmployeeStatus;
import com.inspeedia.toyotsu.lms.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeChangeResult {
    private EmployeeStatus status;
    private Employee employee;
}
