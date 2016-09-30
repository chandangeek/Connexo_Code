package com.elster.jupiter.validation.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiCalculatorHandlerFactory;

public enum TranslationKeys implements TranslationKey {

    MESSAGE_SPEC_SUBSCRIBER(ValidationServiceImpl.SUBSCRIBER_NAME, "Handle data validation"),
    KPICALCULATOR_DISPLAYNAME(DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME)
    ;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    private String key;
    private String defaultFormat;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
