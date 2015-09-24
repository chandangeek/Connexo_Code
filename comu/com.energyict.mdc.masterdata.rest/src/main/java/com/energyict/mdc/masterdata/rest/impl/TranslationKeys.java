package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    TIME_MINUTE("TimeMinute", "%s minute"),
    TIME_MINUTES("TimeMinutes", "%s minutes"),
    TIME_HOUR("TimeHour", "%s hour"),
    TIME_DAY("TimeDay", "%s day"),
    TIME_MONTH("TimeMonth", "%s month"),
    DEVICE_TYPE("com.energyict.mdc.device.config.DeviceType", "Device type"),
    LOADPROFILE_TYPE("com.energyict.mdc.masterdata.LoadProfileType", "Load profile type"),
    REGISTER_TYPE("com.energyict.mdc.masterdata.MeasurementType", "Register type");

    private final String key;
    private final String format;

    TranslationKeys(String key, String format) {
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