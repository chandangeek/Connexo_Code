package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (15:46)
 */
public enum PropertyTranslationKeys implements TranslationKey {

    DEVICE_MRID("device.mRID", "mRID"),
    DEVICE_SERIAL_NUMBER("device.serial.number", "Serial number"),
    DEVICE_TYPE("device.type", "Device type"),
    DEVICE_CONFIGURATION("device.configuration", "Device configuration"),
    DEVICE_STATUS("device.status.name", "state"),
    DEVICE_GROUP("device.group", "Device group"),
    DEVICE_CERT_YEAR("device.cert.year", "Year of certification"),
    BATCH("device.batch", "Batch"),
    DEVICE_DOMAIN("device.search.domain", "Device"),
    DEVICE_DOMAIN_NAME("com.energyict.mdc.device.data.Device", "Device"),
    ;

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
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