package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum HeaderLanguage {
    ENGLISH("en"), JAPANESE("ja");
    private final String name;

    HeaderLanguage(String name) {
        this.name = name;
    }

    public static HeaderLanguage fromString(String name) {
        for (HeaderLanguage language : HeaderLanguage.values()) {
            if (language.getName().equalsIgnoreCase(name)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Invalid language: " + name);
    }
}
