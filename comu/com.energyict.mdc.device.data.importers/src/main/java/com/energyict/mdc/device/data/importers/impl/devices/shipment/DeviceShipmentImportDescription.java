/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
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
        // Device name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Device type
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceType)
                .markMandatory()
                .build());
        // Device configuration
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceConfiguration)
                .markMandatory()
                .build());
        // Manufacturer
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setManufacturer)
                .build());
        // Model number
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setModelNbr)
                .build());
        // Model Version
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setModelVersion)
                .build());
        // Shipment date
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setShipmentDate)
                .markMandatory()
                .build());
        // Serial number
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setSerialNumber)
                .build());

        // Year or certification
        fields.add(CommonField.withParser(new NumberParser())
                .withSetter(number -> record.setYearOfCertification(number != null ? number.intValue() : null))
                .build());
        // Batch
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setBatch)
                .build());
        return fields;
    }
}
