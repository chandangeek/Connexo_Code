package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.DeviceShipmentImporterFactory;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImporterFactory;

public enum TranslationKeys implements TranslationKey {

    DATA_IMPORTER_SUBSCRIBER(DeviceDataImporterMessageHandler.SUBSCRIBER_NAME, "Handle data import"),

    // Properties translations
    DEVICE_DATA_IMPORTER_DELIMITER(DeviceDataImporterProperty.DELIMITER.getPropertyKey(), "Delimiter"),
    DEVICE_DATA_IMPORTER_DATE_FORMAT(DeviceDataImporterProperty.DATE_FORMAT.getPropertyKey(), "Date format"),
    DEVICE_DATA_IMPORTER_TIMEZONE(DeviceDataImporterProperty.TIME_ZONE.getPropertyKey(), "Time zone"),
    DEVICE_DATA_IMPORTER_NUMBER_FORMAT(DeviceDataImporterProperty.NUMBER_FORMAT.getPropertyKey(), "Number format"),

    //Translations for importer names
    DEVICE_READINGS_IMPORTER(DeviceReadingsImporterFactory.NAME, "Device readings importer"),
    DEVICE_SHIPMENT_IMPORTER(DeviceShipmentImporterFactory.NAME, "Register devices from shipment file"),

    // Translations for data columns
    DATA_COLUMN_DEVICE_MRID("DeviceDataImportColumnDeviceMrid", "Device MRID"),
    DATA_COLUMN_DEVICE_TYPE("DeviceDataImportColumnDeviceType", "Device type"),
    DATA_COLUMN_DEVICE_CONFIGURATION("DeviceDataImportColumn", "Device configuration"),
    DATA_COLUMN_SHIPMENT_DATE("DeviceDataImportColumnShipmentDate", "Shipment date"),
    DATA_COLUMN_SERIAL_NUMBER("DeviceDataImportColumn", "Serial number"),
    DATA_COLUMN_YEAR_OF_CERTIFICATION("DeviceDataImportColumnYearOfCertification", "Year of certification"),
    DATA_COLUMN_BATCH("DeviceDataImportColumnBatch", "Batch"),
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