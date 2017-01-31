/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.commission;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class DeviceCommissioningImportDescription implements FileImportDescription<DeviceTransitionRecord> {

    private DateParser dateParser;

    public DeviceCommissioningImportDescription(String dateFormat, String timeZone) {
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceTransitionRecord getFileImportRecord() {
        return new DeviceTransitionRecord();
    }

    public List<FileImportField<?>> getFields(DeviceTransitionRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        //Device mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Transition date
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        // Master device mRID or Name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setMasterDeviceIdentifier)
                .build());
        return fields;
    }
}
