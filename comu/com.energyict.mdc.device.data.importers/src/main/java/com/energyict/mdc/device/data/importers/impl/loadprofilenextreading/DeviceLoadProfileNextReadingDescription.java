/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.loadprofilenextreading;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.google.common.collect.ImmutableMap;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceLoadProfileNextReadingDescription implements FileImportDescription<DeviceLoadProfileNextReadingRecord> {
    private final LiteralStringParser stringParser;
    private final DateParser dateParser;

    public DeviceLoadProfileNextReadingDescription(String dateFormat, String timeZone) {
        stringParser = new LiteralStringParser();
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceLoadProfileNextReadingRecord getFileImportRecord() {
        return new DeviceLoadProfileNextReadingRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceLoadProfileNextReadingRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // LoadProfile OBIS code
        fields.put("loadProfileOBIS", CommonField.withParser(stringParser)
                .withName("Load profile OBIS")
                .withSetter(record::setLoadProfileOBIS)
                .markMandatory()
                .build());
        // Next Reading block start date
        fields.put("nextReadingBlockDateTime", CommonField.withParser(dateParser)
                .withName("Reading date and time")
                .withSetter(record::setLoadProfileNextReadingBlockDateTime)
                .build());

        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                String.class, stringParser,
                ZonedDateTime.class, dateParser
        );
    }}
