package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum BackScreenLabels {
    PAID_LEAVE(0, "Paid leave"),
    SUDDEN_LEAVE(1, "Sudden leave"),
    ADMINISTRATOR(2, "Administrator"),
    EXTERIOR_AND_SORTING(3, "Exterior / Sorting"),
    TAC(4, "Tac"),
    OTHERS(5, "Others");

    private final String value;
    private final int index;
    BackScreenLabels(int index, String value) {
        this.value = value;
        this.index = index;
    }
}
