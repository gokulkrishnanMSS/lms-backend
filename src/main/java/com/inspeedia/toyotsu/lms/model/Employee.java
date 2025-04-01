package com.inspeedia.toyotsu.lms.model;


import jakarta.persistence.*;
import lombok.*;


@Builder
@Entity
@Table(name = "employee")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Employee {

    @Id
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    private String companyName;

    private String photo;

    private String mainProcess;

    private String subProcess;

    private String startDate;

    private String employmentPeriod; // Duration in months/years

    private int qualityCases;

    private long productivity;

    private String suitableArea;

    private int groupId;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillA = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillB = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillC = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillD = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillE = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int skillF = 0;


    public Employee(Employee employee) {
        this.id = employee.id;
        this.name = employee.name;
        this.department = employee.department;
        this.companyName = employee.companyName;
        this.photo = employee.photo;
        this.mainProcess = employee.mainProcess;
        this.subProcess = employee.subProcess;
        this.startDate = employee.startDate;
        this.employmentPeriod = employee.employmentPeriod;
        this.qualityCases = employee.qualityCases;
        this.productivity = employee.productivity;
        this.suitableArea = employee.suitableArea;
        this.groupId = employee.groupId;
        this.isActive = employee.isActive;
        this.skillA = employee.skillA;
        this.skillB = employee.skillB;
        this.skillC = employee.skillC;
        this.skillD = employee.skillD;
        this.skillE = employee.skillE;
        this.skillF = employee.skillF;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMainProcess() {
        return mainProcess;
    }

    public void setMainProcess(String mainProcess) {
        this.mainProcess = mainProcess;
    }

    public String getSubProcess() {
        return subProcess;
    }

    public void setSubProcess(String subProcess) {
        this.subProcess = subProcess;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEmploymentPeriod() {
        return employmentPeriod;
    }

    public void setEmploymentPeriod(String employmentPeriod) {
        this.employmentPeriod = employmentPeriod;
    }

    public int getQualityCases() {
        return qualityCases;
    }

    public void setQualityCases(int qualityCases) {
        this.qualityCases = qualityCases;
    }

    public long getProductivity() {
        return productivity;
    }

    public void setProductivity(long productivity) {
        this.productivity = productivity;
    }

    public String getSuitableArea() {
        return suitableArea;
    }

    public void setSuitableArea(String suitableArea) {
        this.suitableArea = suitableArea;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getSkillA() {
        return skillA;
    }

    public void setSkillA(int skillA) {
        this.skillA = skillA;
    }

    // Getter and Setter for Skill B
    public int getSkillB() {
        return skillB;
    }

    public void setSkillB(int skillB) {
        this.skillB = skillB;
    }

    // Getter and Setter for Skill C
    public int getSkillC() {
        return skillC;
    }

    public void setSkillC(int skillC) {
        this.skillC = skillC;
    }

    // Getter and Setter for Skill D
    public int getSkillD() {
        return skillD;
    }

    public void setSkillD(int skillD) {
        this.skillD = skillD;
    }

    // Getter and Setter for Skill E
    public int getSkillE() {
        return skillE;
    }

    public void setSkillE(int skillE) {
        this.skillE = skillE;
    }

    // Getter and Setter for Skill F
    public int getSkillF() {
        return skillF;
    }

    public void setSkillF(int skillF) {
        this.skillF = skillF;
    }
}
