package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    MISSING_VALUES_VALIDATOR(MissingValuesValidator.class.getName(), "Check missing values"),
    INTERVAL_STATE_VALIDATOR(IntervalStateValidator.class.getName(), "Interval state"),
    REGISTER_INCREASE_VALIDATOR(RegisterIncreaseValidator.class.getName(), "Register increase"),
    THRESHOLD_VALIDATOR(ThresholdValidator.class.getName(), "Threshold violation"),
    ;

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }
}
