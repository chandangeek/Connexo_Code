package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/8/15.
 */
public enum TranslationSeeds implements TranslationKey {
    ID("id", "Identification"),
    MRID("mRID", "mRID"),
    SERIAL_NUMBER("serialNumber", "Serial number"),
    DEVCIETYPE("deviceTypeName", "Device type"),
    DEVICECONFIG("deviceConfigName", "Device configuration"),
    PLUGGEABLE_CLASS("deviceProtocolPluggeableClassId", "Device protocol pluggeable class"),
    YEAR_OF_CERTIFICATION("yearOfCertification", "Year of certification");

    private final String key;
    private final String defaultFormat;

    TranslationSeeds(String key, String defaultFormat) {
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
