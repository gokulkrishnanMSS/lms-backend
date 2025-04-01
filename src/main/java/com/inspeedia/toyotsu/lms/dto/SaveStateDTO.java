package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveStateDTO {
    private List<EmployeeTypeDTO> absentList;
    private List<EmployeeTypeDTO> roasterList;
    private List<ProductionLineType> productionLines;
    private List<EmployeeTypeDTO> breakthroughList;
    private List<EmployeeTypeDTO> administratorList;
    private List<EmployeeTypeDTO> othersList;
}
