package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductionLineType {
    private String id;
    private String lineName;
    private List<SupplierTypeDTO> suppliers;
}