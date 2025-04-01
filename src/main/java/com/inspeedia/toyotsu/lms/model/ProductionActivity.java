package com.inspeedia.toyotsu.lms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="prod_activity")
public class ProductionActivity {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "supplier_id")

    private Employee supplier;
    private String line;
    private LocalDate date;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime workStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime workEndTime;
    @Transient
    private Duration totalWorkTime;
    private String note;
    private String department;
    private String substituteId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubstituteId() {
        return substituteId;
    }

    public void setSubstituteId(String substituteId) {
        this.substituteId = substituteId;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public  String getDepartment(){
        return  this.department;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getSupplier() {
        return supplier;
    }

    public void setSupplier(Employee supplier) {
        this.supplier = supplier;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Duration getTotalWorkTime() {
        return totalWorkTime;
    }

    public void setTotalWorkTime(Duration totalWorkTime) {
        this.totalWorkTime = totalWorkTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @PrePersist
    @PreUpdate
    public void calculateTotalWorkTime() {
        if (workStartTime != null && workEndTime != null) {
            this.totalWorkTime = Duration.between(workStartTime, workEndTime);
        }
    }
}
