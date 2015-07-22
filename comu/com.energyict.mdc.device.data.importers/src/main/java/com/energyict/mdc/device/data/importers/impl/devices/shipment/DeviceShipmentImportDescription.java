package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.parsers.NumberParser;

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
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceType)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceConfiguration)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withConsumer(record::setShipmentDate)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setSerialNumber)
                .build());
        fields.add(CommonField.withParser(new NumberParser())
                .withConsumer(number -> record.setYearOfCertification(number != null ? number.intValue() : null))
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setBatch)
                .build());
        return fields;
    }
}
