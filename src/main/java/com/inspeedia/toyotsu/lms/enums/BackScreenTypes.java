package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum BackScreenTypes {
    ADMINISTRATOR("administrator"),
    OTHERS("others"),
    BREAK("break"),
    TAC("tac"),
    EXTERIOR("exterior"),
    SORTING("sorting");

    private final String value;

    BackScreenTypes(String value) {
        this.value = value;
    }
}
