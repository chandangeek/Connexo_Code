package com.energyict.mdc.device.data.importers.impl.devices.decommission;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImportRecord;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class DeviceDecommissioningImportDescription implements FileImportDescription<DeviceTransitionRecord> {

    private DateParser dateParser;

    public DeviceDecommissioningImportDescription(String dateFormat, String timeZone) {
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceTransitionRecord getFileImportRecord() {
        return new DeviceTransitionRecord();
    }

    public List<FileImportField<?>> getFields(DeviceTransitionRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceMRID)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withConsumer(record::setTransitionDate)
                .markMandatory()
                .build());
        return fields;
    }
}
