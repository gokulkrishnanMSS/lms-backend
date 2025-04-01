package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum Department {
    ARMS("ARMS"),
    PROCAST("PROCAST"),
    TLS("TLS");

    private final String name;
    Department(String name) {
        this.name = name;
    }
}
