package com.inspeedia.toyotsu.lms.dto;


import com.inspeedia.toyotsu.lms.model.Employee;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionActivityDTO {
    private String id;
    private String substituteId;

    @NotBlank(message = "Employee is missing")
    private Employee employee;

    @NotBlank(message = "Department is missing")
    private String department;

    @NotBlank(message = "Date is missing")
    private String date;

    @NotBlank(message = "Supplier is missing")
    private Employee supplier;

    @NotBlank(message = "Line is missing")
    private String line;

    @NotBlank(message = "Work start time is missing")
    private String workStartTime;

    @NotBlank(message = "Work end time is missing")
    private String workEndTime;

    @NotBlank(message = "note is missing")
    private String note;

    @NotBlank(message = "Total work time is missing")
    private String totalWorkTime;

}
