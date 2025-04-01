package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SupplierTypeDTO {
    private String id;
    private EmployeeTypeDTO rosterItem;
    private List<ProductionLineAreaDTO> packers;
}
