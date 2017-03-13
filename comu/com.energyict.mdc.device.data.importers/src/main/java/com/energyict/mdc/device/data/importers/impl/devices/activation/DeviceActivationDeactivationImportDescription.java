/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivationDeactivationImportDescription implements FileImportDescription<DeviceActivationDeactivationRecord> {

    private DateParser dateParser;

    public DeviceActivationDeactivationImportDescription(String dateFormat, String timeZone) {
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceActivationDeactivationRecord getFileImportRecord() {
        return new DeviceActivationDeactivationRecord();
    }

    public List<FileImportField<?>> getFields(DeviceActivationDeactivationRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        // Device mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Transition date
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        // Activation flag
        fields.add(CommonField.withParser(new BooleanParser())
                .withSetter(record::setActivate)
                .markMandatory()
                .build());
        return fields;
    }
}
