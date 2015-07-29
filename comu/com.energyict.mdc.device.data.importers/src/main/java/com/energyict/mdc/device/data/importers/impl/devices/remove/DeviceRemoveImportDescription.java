package com.energyict.mdc.device.data.importers.impl.devices.remove;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImportRecord;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class DeviceRemoveImportDescription implements FileImportDescription<DeviceTransitionRecord> {

    public DeviceRemoveImportDescription() {}

    @Override
    public DeviceTransitionRecord getFileImportRecord() {
        return new DeviceTransitionRecord();
    }

    public List<FileImportField<?>> getFields(DeviceTransitionRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        fields.add(CommonField.withParser(new LiteralStringParser())
                .withConsumer(record::setDeviceMRID)
                .markMandatory()
                .build());
        return fields;
    }
}
