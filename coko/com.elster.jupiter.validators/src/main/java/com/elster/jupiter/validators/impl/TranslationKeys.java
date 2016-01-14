package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    MISSING_VALUES_VALIDATOR(MissingValuesValidator.class.getName(), "Check missing values"),
    INTERVAL_STATE_VALIDATOR(IntervalStateValidator.class.getName(), "Interval state"),
    INTERVAL_STATE_VALIDATOR_INTERVAL_FLAGS(IntervalStateValidator.class.getName() + "." + IntervalStateValidator.INTERVAL_FLAGS, "Interval flags"),
    REGISTER_INCREASE_VALIDATOR(RegisterIncreaseValidator.class.getName(), "Register increase"),
    REGISTER_INCREASE_VALIDATOR_FAIL_EQUAL_DATA(RegisterIncreaseValidator.class.getName() + "." + RegisterIncreaseValidator.FAIL_EQUAL_DATA, "Fail equal data"),
    THRESHOLD_VALIDATOR(ThresholdValidator.class.getName(), "Threshold violation"),
    THRESHOLD_VALIDATOR_MIN(ThresholdValidator.class.getName() + "." + ThresholdValidator.MIN, "Minimum"),
    THRESHOLD_VALIDATOR_MAX(ThresholdValidator.class.getName() + "." + ThresholdValidator.MAX, "Maximum"),
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
