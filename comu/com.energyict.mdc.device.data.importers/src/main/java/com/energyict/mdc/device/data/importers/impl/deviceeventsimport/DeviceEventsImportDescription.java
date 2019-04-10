package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

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

public class DeviceEventsImportDescription implements FileImportDescription<DeviceEventsImportRecord> {
    private final LiteralStringParser stringParser;
    private final DateParser dateParser;

    public DeviceEventsImportDescription(String dateFormat, String timeZone){
        stringParser = new LiteralStringParser();
        this.dateParser = new DateParser(dateFormat, timeZone);
    }

    @Override
    public DeviceEventsImportRecord getFileImportRecord() {
        return new DeviceEventsImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(DeviceEventsImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        fields.put("date", CommonField.withParser(dateParser)
                .withName("Date")
                .withSetter(record::setDateTime)
                .markMandatory()
                .build());
        // LoadProfile OBIS code
        fields.put("logbookOBIScode", CommonField.withParser(stringParser)
                .withName("Logbook OBIS code")
                .withSetter(record::setLogbookOBIScode)
                .markMandatory()
                .build());
        // Event type CIM
        fields.put("eventCode", CommonField.withParser(stringParser)
                .withName("Event CIM Code")
                .withSetter(record::setEventCode)
                .markMandatory()
                .build());
        // Optional fields:
        fields.put("description", CommonField.withParser(stringParser)
                .withName("Description")
                .withSetter(record::setDescription)
                .build());
        fields.put("eventLogID", CommonField.withParser(stringParser)
                .withName("Event Log ID")
                .withSetter(record::setEventLogID)
                .build());
        fields.put("deviceCode", CommonField.withParser(stringParser)
                .withName("Device code")
                .withSetter(record::setDeviceCode)
                .build());
        fields.put("readingDate", CommonField.withParser(dateParser)
                .withName("Reading date")
                .withSetter(record::setReadingDate)
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
