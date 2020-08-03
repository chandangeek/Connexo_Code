/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceTopologyImportDescription implements FileImportDescription<DeviceTopologyImportRecord> {

    private LiteralStringParser stringParser;

    public DeviceTopologyImportDescription() {
        stringParser = new LiteralStringParser();
    }

    @Override
    public DeviceTopologyImportRecord getFileImportRecord() {
        return new DeviceTopologyImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceTopologyImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Master device serial number or name
        fields.put("masterDeviceIdentifier", CommonField.withParser(stringParser)
                .withName("masterDevice")
                .withSetter(record::setMasterDeviceIdentifier)
                .build());
        // Slave device serial number or name
        fields.put("slaveDeviceIdentifier", CommonField.withParser(stringParser)
                .withName("slaveDevice")
                .withSetter(record::setSlaveDeviceIdentifier)
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser
        );
    }
}
