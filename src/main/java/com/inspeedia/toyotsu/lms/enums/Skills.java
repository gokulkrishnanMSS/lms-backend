package com.inspeedia.toyotsu.lms.enums;

public enum Skills {
    A("skill A"),
    B("skill B"),
    C("skill C"),
    D("skill D"),
    E("skill E"),
    F("skill F");

    private final String label;

    Skills(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
