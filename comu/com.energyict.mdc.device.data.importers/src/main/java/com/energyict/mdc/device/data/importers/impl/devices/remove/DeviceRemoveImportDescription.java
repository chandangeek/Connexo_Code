/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.remove;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceRemoveImportDescription implements FileImportDescription<DeviceTransitionRecord> {
    private LiteralStringParser stringParser;

    public DeviceRemoveImportDescription() {
        stringParser = new LiteralStringParser();
    }

    @Override
    public DeviceTransitionRecord getFileImportRecord() {
        return new DeviceTransitionRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceTransitionRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        //Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device Identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return Collections.singletonMap(String.class, stringParser);
    }
}
