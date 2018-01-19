/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.parsers.NumberParser;

import com.google.common.collect.ImmutableMap;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceShipmentImportDescription implements FileImportDescription<DeviceShipmentImportRecord> {
    private LiteralStringParser stringParser;
    private NumberParser numberParser;
    private DateParser dateParser;

    public DeviceShipmentImportDescription(String dateFormat, String timeZone) {
        stringParser = new LiteralStringParser();
        numberParser = new NumberParser();
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceShipmentImportRecord getFileImportRecord() {
        return new DeviceShipmentImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceShipmentImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device Identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Device type
        fields.put("deviceType", CommonField.withParser(stringParser)
                .withName("Device type")
                .withSetter(record::setDeviceType)
                .markMandatory()
                .build());
        // Device configuration
        fields.put("deviceConfiguration", CommonField.withParser(stringParser)
                .withName("Device configuration")
                .withSetter(record::setDeviceConfiguration)
                .markMandatory()
                .build());
        // Manufacturer
        fields.put("manufacturer", CommonField.withParser(stringParser)
                .withName("Manufacturer")
                .withSetter(record::setManufacturer)
                .build());
        // Model number
        fields.put("modelNumber", CommonField.withParser(stringParser)
                .withName("Model number")
                .withSetter(record::setModelNbr)
                .build());
        // Model Version
        fields.put("modelVersion", CommonField.withParser(stringParser)
                .withName("Model version")
                .withSetter(record::setModelVersion)
                .build());
        // Shipment date
        fields.put("shipmentDate", CommonField.withParser(dateParser)
                .withName("Shipment date")
                .withSetter(record::setShipmentDate)
                .markMandatory()
                .build());
        // Serial number
        fields.put("serialNumber", CommonField.withParser(stringParser)
                .withName("Serial number")
                .withSetter(record::setSerialNumber)
                .build());
        // Year or certification
        fields.put("yearOfCertification", CommonField.withParser(numberParser)
                .withName("Year of certification")
                .withSetter(number -> record.setYearOfCertification(number != null ? number.intValue() : null))
                .build());
        // Batch
        fields.put("batch", CommonField.withParser(stringParser)
                .withName("Batch")
                .withSetter(record::setBatch)
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser,
                Number.class, numberParser,
                ZonedDateTime.class, dateParser
        );
    }
}
