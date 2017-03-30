/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.elster.jupiter.metering.LocationTemplate.TemplateField;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DeviceInstallationImportDescription implements FileImportDescription<DeviceInstallationImportRecord> {

    private DateParser dateParser;
    private volatile DeviceDataImporterContext context;

    public DeviceInstallationImportDescription(String dateFormat, String timeZone, DeviceDataImporterContext context) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.context = context;
    }

    @Override
    public DeviceInstallationImportRecord getFileImportRecord() {
        return new DeviceInstallationImportRecord();
    }

    public List<FileImportField<?>> getFields(DeviceInstallationImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        BigDecimalParser bigDecimalParser = new BigDecimalParser(SupportedNumberFormat.FORMAT3);
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
        // Geo coordinates
        IntStream.range(0, 3).forEach(cnt ->
                fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setGeoCoordinates)
                .build()));
        context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                .sorted((t1,t2)->Integer.compare(t1.getRanking(),t2.getRanking()))
                .map(TemplateField::getName)
                .forEach(s-> fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addLocation)
                        .build()));
        // Master device mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setMasterDeviceIdentifier)
                .build());
        // Usage point mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setUsagePointIdentifier)
                .build());
        // Service category
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setServiceCategory)
                .build());
        // Install inactive flag
        fields.add(CommonField.withParser(new BooleanParser())
                .withSetter(record::setInstallInactive)
                .build());
        // Start validation date
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setStartValidationDate)
                .build());
        // Multiplier
        fields.add(CommonField.withParser(bigDecimalParser)
                .withSetter(record::setMultiplier)
                .build());
        return fields;
    }
}
