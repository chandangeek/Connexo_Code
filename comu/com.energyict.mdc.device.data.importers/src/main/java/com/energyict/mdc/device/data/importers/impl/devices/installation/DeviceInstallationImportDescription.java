package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class DeviceInstallationImportDescription implements FileImportDescription<DeviceInstallationImportRecord> {

    private DateParser dateParser;

    public DeviceInstallationImportDescription(String dateFormat, String timeZone) {
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceInstallationImportRecord getFileImportRecord() {
        return new DeviceInstallationImportRecord();
    }

    public List<FileImportField<?>> getFields(DeviceInstallationImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceMRID)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setMasterDeviceMrid)
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setUsagePointMrid)
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setServiceCategory)
                .build());
        fields.add(CommonField.withParser(new BooleanParser())
                .withSetter(record::setInstallInactive)
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setStartValidationDate)
                .build());
        return fields;
    }
}
