/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiCalculatorHandlerFactory;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    KPICALCULATOR_DISPLAYNAME(DataQualityKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataQualityKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME);

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