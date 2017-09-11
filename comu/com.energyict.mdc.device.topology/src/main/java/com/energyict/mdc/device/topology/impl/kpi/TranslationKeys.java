/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    REGISTERED_DEVICES_KPI_CALCULATOR(RegisteredDevicesKpiCalculatorFactory.TASK_SUBSCRIBER, RegisteredDevicesKpiCalculatorFactory.TASK_SUBSCRIBER_DISPLAYNAME),
    ;

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
