/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;


public enum TaskTypesTranslationKeys implements TranslationKey {
    DATAVALIDATION("DataValidation", "Data validation"),
    ESTIMATIONTASK("EstimationTask", "Data estimation"),
    DATAEXPORT("DataExport", "Data export"),
    MDCKPIREGISTEREDDEVTOPIC("MDCKpiRegisteredDevTopic", "Registered devices KPI"),
    MDCKPICALCULATORTOPIC("MDCKpiCalculatorTopic", "Data collection KPI"),
    DATAQUALITYKPICALCTOPIC("DataQualityKpiCalcTopic", "Data quality KPI");

    private String key;
    private String defaultFormat;

    TaskTypesTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getTranslated(Thesaurus thesaurus, Object... args) {
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        return thesaurus.getFormat(this).format(args);
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
