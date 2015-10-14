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

    DEVICE_MRID("device.mRID", "MRID"),
    DEVICE_SERIAL_NUMBER("device.serial.number", "Serial number"),
    DEVICE_TYPE("device.type", "Device type"),
    DEVICE_CONFIGURATION("device.configuration", "Device configuration"),
    DEVICE_STATUS("device.status.name", "State"),
    DEVICE_GROUP("device.group", "Device group"),
    DEVICE_CERT_YEAR("device.cert.year", "Year of certification"),
    BATCH("device.batch", "Batch"),
    CONNECTION_METHOD("device.connection.method", "Connection method"),
    SHARED_SCHEDULE("device.shared.schedule", "Shared schedule"),
    USAGE_POINT("device.usage.point", "Usage point"),
    SERVICE_CATEGORY("device.service.category", "Service category"),
    TOPOLOGY("device.topology", "Topology"),
    DEVICE_MASTER_MRID("device.master.mrid", "Master device"),
    DEVICE_SLAVE_MRID("device.slave.mrid", "Slave device"),
    VALIDATION("device.validation", "Validation"),
    VALIDATION_STATUS("device.validation.status", "Validation active"),
    ESTIMATION("device.estimation", "Estimation"),
    ESTIMATION_STATUS("device.estimation.status", "Estimation active"),
    SECURITY("device.security", "Security"),
    SECURITY_NAME("device.security.name", "Name")
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