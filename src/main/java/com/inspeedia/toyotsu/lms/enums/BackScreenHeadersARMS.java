package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum BackScreenHeadersARMS {
    BREAK("break", new String[]{"Sudden leave(leave work midway)", "突発休(途中退勤)"}),
    ADMINISTRATOR("administrator", new String[]{"Administrator", "管理者"}),
    EXTERIOR("exterior", new String[]{"Exterior/sorting", "外装/仕分け"}),
    SORTING("sorting", new String[]{"Exterior/sorting", "外装/仕分け"}),
    OTHERS("others", new String[]{"Other", "その他"});

    private final String name;
    private final String[] translations;

    BackScreenHeadersARMS(String name, String[] translations) {
        this.name = name;
        this.translations = translations;
    }

    public String getTranslation(HeaderLanguage language) {
        return translations[language.ordinal()];
    }
}
