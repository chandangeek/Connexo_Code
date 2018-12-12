/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import com.google.common.collect.ImmutableMap;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceActivationDeactivationImportDescription implements FileImportDescription<DeviceActivationDeactivationRecord> {
    private LiteralStringParser stringParser;
    private DateParser dateParser;

    public DeviceActivationDeactivationImportDescription(String dateFormat, String timeZone) {
        stringParser = new LiteralStringParser();
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceActivationDeactivationRecord getFileImportRecord() {
        return new DeviceActivationDeactivationRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceActivationDeactivationRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device Identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Transition date
        fields.put("transitionDate", CommonField.withParser(dateParser)
                .withName("Transition date")
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        // Activation flag
        fields.put("activate", CommonField.withParser(new BooleanParser())
                .withName("Activate")
                .withSetter(record::setActivate)
                .markMandatory()
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser,
                ZonedDateTime.class, dateParser
        );
    }
}
