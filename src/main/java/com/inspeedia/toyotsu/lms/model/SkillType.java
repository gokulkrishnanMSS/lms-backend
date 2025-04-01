package com.inspeedia.toyotsu.lms.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inspeedia.toyotsu.lms.enums.Skills;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkillType {
    private String label;
    private Map<Skills, Integer> categories;

    public SkillType(String label) {
        this.label = label;
        this.categories = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        // Initialize all skills with a default value of 0 in the desired order
        for (Skills skill : Skills.values()) {
            this.categories.put(skill, 0);
        }
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("categories")
    public Map<Skills, Integer> getCategories() {
        return categories;
    }

    public void setSkillLevel(Skills skill, int level) {
        this.categories.put(skill, level);
    }
}