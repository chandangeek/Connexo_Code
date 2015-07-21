package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.parsers.NumberParser;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeviceShipmentImportDescription implements FileImportDescription<DeviceShipmentImportRecord> {

    private DateParser dateParser;

    public DeviceShipmentImportDescription(String dateFormat, String timeZone) {
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceShipmentImportRecord getFileImportRecord() {
        return new DeviceShipmentImportRecord();
    }

    public List<FileImportField<?>> getFields(DeviceShipmentImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceMRID)
                .markMandatory()
                .onColumn(TranslationKeys.DATA_COLUMN_DEVICE_MRID.getKey())
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceType)
                .markMandatory()
                .onColumn(TranslationKeys.DATA_COLUMN_DEVICE_TYPE.getKey())
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceConfiguration)
                .markMandatory()
                .onColumn(TranslationKeys.DATA_COLUMN_DEVICE_CONFIGURATION.getKey())
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withConsumer(record::setShipmentDate)
                .markMandatory()
                .onColumn(TranslationKeys.DATA_COLUMN_SHIPMENT_DATE.getKey())
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setSerialNumber)
                .onColumn(TranslationKeys.DATA_COLUMN_SERIAL_NUMBER.getKey())
                .build());
        fields.add(CommonField.withParser(new NumberParser())
                .withConsumer(number -> record.setYearOfCertification(number != null ? number.intValue() : null))
                .onColumn(TranslationKeys.DATA_COLUMN_YEAR_OF_CERTIFICATION.getKey())
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setBatch)
                .onColumn(TranslationKeys.DATA_COLUMN_BATCH.getKey())
                .build());
        return fields;
    }
}
