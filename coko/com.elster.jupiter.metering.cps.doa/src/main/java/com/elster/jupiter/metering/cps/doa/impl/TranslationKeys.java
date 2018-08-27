/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    DOMAIN_NAME_USAGEPOINT("doa.name.usagePoint", "Usage point"),
    DOA_CPS_GENERAL_NAME("doa.name.general", "General"),
    DOA_CPS_COMSUMPTION_ALLOCATION_NAME("doa.name.consumptionAllocation","Consumption allocation"),
     ;

    private String key;
    private String defaultFormat;

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
