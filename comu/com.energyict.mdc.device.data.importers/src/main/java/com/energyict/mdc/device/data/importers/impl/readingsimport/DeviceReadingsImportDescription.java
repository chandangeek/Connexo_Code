/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceReadingsImportDescription implements FileImportDescription<DeviceReadingsImportRecord> {
    private final LiteralStringParser stringParser;
    private final DateParser dateParser;
    private final BigDecimalParser bigDecimalParser;

    public DeviceReadingsImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat) {
        stringParser = new LiteralStringParser();
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
    }

    @Override
    public DeviceReadingsImportRecord getFileImportRecord() {
        return new DeviceReadingsImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceReadingsImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Reading date
        fields.put("readingDateTime", CommonField.withParser(dateParser)
                .withName("Reading date and time")
                .withSetter(record::setReadingDateTime)
                .markMandatory()
                .build());
        // Reading type mRID
        fields.put("readingType", CommonField.withParser(stringParser)
                .withName("Reading type")
                .withSetter(record::addReadingType)
                .markMandatory()
                .markRepetitive()
                .build());
        // Reading value
        fields.put("readingValue", CommonField.withParser(bigDecimalParser)
                .withName("Reading value")
                .withSetter(record::addReadingValue)
                .markMandatory()
                .markRepetitive()
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser,
                BigDecimal.class, bigDecimalParser,
                ZonedDateTime.class, dateParser
        );
    }}
