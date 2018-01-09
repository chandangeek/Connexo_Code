/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
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

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DeviceInstallationImportDescription implements FileImportDescription<DeviceInstallationImportRecord> {
    private LiteralStringParser stringParser;
    private BooleanParser booleanParser;
    private BigDecimalParser bigDecimalParser;
    private DateParser dateParser;
    private volatile DeviceDataImporterContext context;

    public DeviceInstallationImportDescription(String dateFormat, String timeZone, DeviceDataImporterContext context) {
        stringParser = new LiteralStringParser();
        booleanParser = new BooleanParser();
        bigDecimalParser = new BigDecimalParser(SupportedNumberFormat.FORMAT3);
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.context = context;
    }

    @Override
    public DeviceInstallationImportRecord getFileImportRecord() {
        return new DeviceInstallationImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceInstallationImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Transition date
        fields.put("transitionDate", CommonField.withParser(dateParser)
                .withName("Transition date")
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        // Geo coordinates
        Stream.of("Latitude", "Longitude", "Elevation").forEach(name ->
                fields.put("geoCoordinate" + name, CommonField.withParser(stringParser)
                        .withName(name)
                        .withSetter(record::setGeoCoordinates)
                        .build()));
        context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                .sorted(Comparator.comparingInt(TemplateField::getRanking))
                .map(TemplateField::getName)
                .forEach(name -> fields.put("locationField" + name, CommonField.withParser(stringParser)
                        .withName("Location field " + name)
                        .withSetter(record::addLocation)
                        .build()));
        // Master device mRID or name
        fields.put("masterDeviceIdentifier", CommonField.withParser(stringParser)
                .withName("Master device identifier")
                .withSetter(record::setMasterDeviceIdentifier)
                .build());
        // Usage point mRID or name
        fields.put("usagePointIdentifier", CommonField.withParser(stringParser)
                .withName("Usage point identifier")
                .withSetter(record::setUsagePointIdentifier)
                .build());
        // Service category
        fields.put("serviceCategory", CommonField.withParser(stringParser)
                .withName("Service category")
                .withSetter(record::setServiceCategory)
                .build());
        // Install inactive flag
        fields.put("installInactive", CommonField.withParser(booleanParser)
                .withName("Install inactive")
                .withSetter(record::setInstallInactive)
                .build());
        // Start validation date
        fields.put("startValidationDate", CommonField.withParser(dateParser)
                .withName("Start validation date")
                .withSetter(record::setStartValidationDate)
                .build());
        // Multiplier
        fields.put("multiplier", CommonField.withParser(bigDecimalParser)
                .withName("Multiplier")
                .withSetter(record::setMultiplier)
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser,
                Boolean.class, booleanParser,
                BigDecimal.class, bigDecimalParser,
                ZonedDateTime.class, dateParser
        );
    }
}
