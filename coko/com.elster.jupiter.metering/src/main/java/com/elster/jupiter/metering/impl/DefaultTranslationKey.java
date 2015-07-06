package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey{
    PRIVILEGE_USAGE_POINT_NAME("metering.usage.points", "Usage points"),
    PRIVILEGE_USAGE_POINT_DESCRIPTION("metering.usage.points.description", "Administrate usage points"),
    ;

    private String key;
    private String format;

    DefaultTranslationKey(String key, String format) {
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }
}
