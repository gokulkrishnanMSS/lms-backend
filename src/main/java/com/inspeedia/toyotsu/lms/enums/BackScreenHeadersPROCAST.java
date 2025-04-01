package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum BackScreenHeadersPROCAST {
    PAID_LEAVE("paidLeave", new String[]{"Paid leave", "有給"}),
    BREAK("break", new String[]{"Sudden leave(leave work midway)", "突発休(途中退勤)"}),
    ADMINISTRATOR("administrator", new String[]{"Administrator", "管理者"}),
    SORTING("sorting", new String[]{"Exterior/sorting", "外装/仕分け"}),
    EXTERIOR("exterior", new String[]{"Exterior/sorting", "外装/仕分け"}),
    OTHERS("others", new String[]{"Other", "その他"});

    private final String name;
    private final String[] translations;

    BackScreenHeadersPROCAST(String name, String[] translations) {
        this.name = name;
        this.translations = translations;
    }

    public String getTranslation(HeaderLanguage language) {
        return translations[language.ordinal()];
    }
}
