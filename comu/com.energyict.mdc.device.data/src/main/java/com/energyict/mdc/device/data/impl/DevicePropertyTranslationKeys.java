/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DevicePropertyTranslationKeys implements TranslationKey {

    DEVICE_DOMAIN_NAME("com.energyict.mdc.device.data.Device", "Device"),
    ;

    private String key;
    private String defaultFormat;

    DevicePropertyTranslationKeys(String key, String defaultFormat) {
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