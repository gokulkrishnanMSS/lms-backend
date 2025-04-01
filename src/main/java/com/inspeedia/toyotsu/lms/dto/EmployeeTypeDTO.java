package com.inspeedia.toyotsu.lms.dto;

import lombok.Data;

@Data
public class EmployeeTypeDTO {
    private String cardId;
    private String fullName;
    private String affiliation;
    private String companyName;
    private String photograph;
    private String mainProcess;
    private String subOperation;
    private boolean assigned;
    private String employmentPeriod;
    private String qualityGrade;
    private String qualityResult;
    private String productivityGrade;
    private String productivityPercentage;
    private String suitableArea;
    private String group;
    private String skillGraph;
    private String impNotes;
    private String role;
}
