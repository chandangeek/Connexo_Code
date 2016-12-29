package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    AND("andSeparator", "and"),
    DAY_LIMIT_NO_CASE("dayLimitNoCase", "day limit"),
    WEEK_LIMIT_NO_CASE("weekLimitNoCase", "week limit"),
    MONTH_LIMIT_NO_CASE("monthLimitNoCase", "month limit");


    private final String key;
    private final String description;

    TranslationKeys(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return description;
    }
}
