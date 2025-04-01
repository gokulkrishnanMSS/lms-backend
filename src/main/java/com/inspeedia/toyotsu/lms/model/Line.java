package com.inspeedia.toyotsu.lms.model;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.HashMap;
import java.util.Map;

public class Line {
    private String name;

    @Override
    public String toString() {
        return "Line{" + "name='" + name + '\'' + ", supplierCount=" + supplierCount + ", operatorCounts=" + operatorCounts + '}';
    }

    private int supplierCount;
    private Map<Integer,Integer> operatorCounts = new HashMap<>();

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(int supplierCount) {
        this.supplierCount = supplierCount;
    }

    public void addOperatorCount(Integer supplier, int count) {
        operatorCounts.put(supplier, count);
    }

    public Map<Integer, Integer> getOperatorCounts() {
        return operatorCounts;
    }
}
