package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey {
    //Resources
    RESOURCE_VALIDATION_CONFIGURATION("usagePoint.config.validationConfiguration", "Validation configuration"),
    RESOURCE_VALIDATION_CONFIGURATION_DESCRIPTION("usagePoint.config.validationConfiguration.description", "Manage validation configuration");

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