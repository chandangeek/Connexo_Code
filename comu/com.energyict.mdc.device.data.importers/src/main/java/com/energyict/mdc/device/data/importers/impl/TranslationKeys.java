package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImporterFactory;

public enum TranslationKeys implements TranslationKey {

    DATA_IMPORTER_SUBSCRIBER(DeviceDataImporterMessageHandler.SUBSCRIBER_NAME, "Handle data import"),

    //Device Readings Importer's translation keys
    DEVICE_READINGS_IMPORTER(DeviceReadingsImporterFactory.NAME, "Device readings importer"),
    DEVICE_READINGS_IMPORTER_DELIMITER(DeviceReadingsImporterFactory.DELIMITER, "Delimiter"),
    DEVICE_READINGS_IMPORTER_DATEFORMAT(DeviceReadingsImporterFactory.DATE_FORMAT, "Date format"),
    DEVICE_READINGS_IMPORTER_TIMEZONE(DeviceReadingsImporterFactory.TIME_ZONE, "Time zone"),
    DEVICE_READINGS_IMPORTER_NUMBERFORMAT(DeviceReadingsImporterFactory.NUMBER_FORMAT, "Number format")
    ;

    private final String key;
    private final String defaultFormat;

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