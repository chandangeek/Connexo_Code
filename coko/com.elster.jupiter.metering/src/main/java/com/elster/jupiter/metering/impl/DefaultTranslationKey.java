package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey{
    PRIVILEGE_USAGE_POINT_NAME("metering.usage.points", "Usage points"),
    PRIVILEGE_USAGE_POINT_DESCRIPTION("metering.usage.points.description", "Administer usage points"),
    PRIVILEGE_READING_TYPE_NAME("metering.reading.types", "Reading types"),
    PRIVILEGE_READING_TYPE_DESCRIPTION("metering.reading.types.description", "Administer reading types"),
    PRIVILEGE_SERVICE_CATEGORY_NAME("metering.service.category", "Service categories"),
    PRIVILEGE_SERVICE_CATEGORY_DESCRIPTION("metering.service.category.description", "Administer service categories"),
    SUBSCRIBER_TRANSLATION(SwitchStateMachineEvent.SUBSCRIBER, SwitchStateMachineEvent.SUBSCRIBER_TRANSLATION),
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