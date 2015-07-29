package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImportRecord;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
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
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceMRID)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withConsumer(record::setTransitionDate)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(new BooleanParser())
                .withConsumer(record::setActivate)
                .markMandatory()
                .build());
        return fields;
    }
}
