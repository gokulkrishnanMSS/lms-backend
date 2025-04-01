package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SupplierDTO {
    private String id;
    private EmployeeDTO rosterItem;
    private List<ProductionLineAreaDTO> packers;
}