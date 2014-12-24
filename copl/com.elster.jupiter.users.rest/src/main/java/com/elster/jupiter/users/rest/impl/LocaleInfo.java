package com.elster.jupiter.users.rest.impl;

import java.util.Locale;

public class LocaleInfo {
    
    public String languageTag;
    public String displayValue;
    
    public LocaleInfo() {
    }
    
    public LocaleInfo(Locale locale, Locale displayLocale) {
        this.languageTag = locale.toLanguageTag();
        this.displayValue = locale.getDisplayName(displayLocale);
    }
}