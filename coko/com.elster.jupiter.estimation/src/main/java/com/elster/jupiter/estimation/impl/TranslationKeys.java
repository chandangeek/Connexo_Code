package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    ESTIMATIONS_PRIVILEGE_CATEGORY_NAME ("estimation.estimations", "Estimation"),
    ESTIMATIONS_PRIVILEGE_CATEGORY_DESCRIPTION("estimation.estimations.description", "Estimation"),
    RELATIVE_PERIOD_CATEGORY("relativeperiod.category.estimation", "Estimation"),
    SUBSCRIBER_NAME(EstimationServiceImpl.SUBSCRIBER_NAME, EstimationServiceImpl.SUBSCRIBER_DISPLAYNAME),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
