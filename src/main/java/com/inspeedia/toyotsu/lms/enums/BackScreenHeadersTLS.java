package com.inspeedia.toyotsu.lms.enums;

import lombok.Getter;

@Getter
public enum BackScreenHeadersTLS {
    PAID_LEAVE("paidLeave", new String[]{"Paid leave", "有給"}),
    BREAK("break", new String[]{"Sudden leave(leave work midway)", "突発休(途中退勤)"}),
    ADMINISTRATOR("administrator", new String[]{"Administrator", "管理者"}),
    TAC("tac", new String[]{"tac", "tac"}),
    OTHERS("others", new String[]{"Other", "その他"});

    private final String name;
    private final String[] translations;

    BackScreenHeadersTLS(String name, String[] translations) {
        this.name = name;
        this.translations = translations;
    }

    public String getTranslation(HeaderLanguage language) {
        return translations[language.ordinal()];
    }
}
