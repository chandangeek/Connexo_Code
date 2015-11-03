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

    DEVICE_DOMAIN("device.search.domain", "Device"),
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
    SECURITY_NAME("device.security.name", "Name"),
    REGISTER("device.register", "Register"),
    REGISTER_OBISCODE("device.register.obiscode", "OBIS code"),
    REGISTER_LAST_READING("device.register.last.reading", "Last reading"),
    READING_TYPE_NAME("reading.type.name", "Reading type: Name"),
    READING_TYPE_UNIT_OF_MEASURE("reading.type.unit.of.measure", "Reading type: Unit of measure"),
    READING_TYPE_TOU("reading.type.tou", "Reading type: Time of use"),
    PROTOCOL_DIALECT("protocol.dialect", "Protocol dialect"),
    CHANNEL("device.channel", "Channel"),
    CHANNEL_OBISCODE("device.channel.obiscode", "OBIS code"),
    CHANNEL_INTERVAL("device.channel.interval", "Interval"),
    LOGBOOK("device.logbook", "Logbook"),
    LOGBOOK_NAME("device.logbook.name", "Name"),
    LOGBOOK_OBISCODE("device.logbook.obiscode", "OBIS code"),
    LOADPROFILE("device.loadprofile", "Load profile"),
    LOADPROFILE_NAME("device.loadprofile.name", "Name"),
    COMTASK("device.comtask", "Communication task"),
    COMTASK_NAME("device.comtask.name", "Name"),
    COMTASK_SECURITY_SETTING("device.comtask.security", "Security settings"),
    PROTOCOL_DIALECT_DYNAMIC_PROP("protocol.dialect.dynamic", "Protocol dialect dynamic properties"),
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