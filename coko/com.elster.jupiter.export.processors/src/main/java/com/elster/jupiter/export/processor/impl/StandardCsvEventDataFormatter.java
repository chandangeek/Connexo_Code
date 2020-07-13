/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.TextLineExportData;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class StandardCsvEventDataFormatter implements StandardFormatter {

    private static final String SEMICOLON_SEPARATOR = ";";
    private static final String COMMA_SEPARATOR = ",";
    private final DataExportService dataExportService;
    private String separator;
    private String tag;
    private boolean withDeviceCode;
    private boolean withDescription;

    StandardCsvEventDataFormatter(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    private StandardCsvEventDataFormatter init(TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag, boolean withDeviceCode, boolean withDescription) {
        this.separator = defineSeparator(translatablePropertyValueInfo);
        this.tag = tag;
        this.withDeviceCode = withDeviceCode;
        this.withDescription = withDescription;
        return this;
    }

    static StandardCsvEventDataFormatter from(DataExportService dataExportService, TranslatablePropertyValueInfo translatablePropertyValueInfo,
                                              String tag, boolean withDeviceCode, boolean withDescription) {
        return new StandardCsvEventDataFormatter(dataExportService).init(translatablePropertyValueInfo, tag, withDeviceCode, withDescription);
    }

    @Override
    public void startExport(DataExportOccurrence occurrence, Logger logger) {
    }

    @Override
    public FormattedData processData(Stream<ExportData> data) {
        StructureMarker rootStructureMarker = dataExportService.forRoot(tag);
        return SimpleFormattedData.of(processToLines(data, rootStructureMarker));
    }

    private List<FormattedExportData> processToLines(Stream<ExportData> data, StructureMarker rootStructureMarker) {
        return decorate(data)
                .filterSubType(MeterEventData.class)
                .flatMap(meterEventData -> {
                    StructureMarker structureMarker = meterEventData.getStructureMarker();
                    return meterEventData.getMeterReading().getEvents().stream()
                            .map(endDeviceEvent -> formatPayload(endDeviceEvent, structureMarker))
                            .map(payload -> TextLineExportData.of(
                                    rootStructureMarker.adopt(structureMarker).withPeriodOf(structureMarker),
                                    payload));
                })
                .collect(Collectors.toList());
    }

    private String formatPayload(EndDeviceEvent endDeviceEvent, StructureMarker structureMarker) {
        ZonedDateTime eventTime = ZonedDateTime.ofInstant(endDeviceEvent.getCreatedDateTime(), ZoneId.systemDefault());
        StringJoiner joiner = new StringJoiner(separator, "", "\n")
                .add(DEFAULT_DATE_TIME_FORMAT.format(eventTime))
                .add(endDeviceEvent.getEventTypeCode());
        if (withDeviceCode) {
            String deviceCode = endDeviceEvent.getType();
            joiner.add(deviceCode != null ? deviceCode : "");
        }
        if (withDescription) {
            String description = endDeviceEvent.getDescription();
            joiner.add(description != null ? description : "");
        }
        // adding list of device identifiers; see com.elster.jupiter.export.impl.EventSelector.buildStructureMarker
        structureMarker.getStructurePath().forEach(joiner::add);
        return joiner.toString();
    }

    @Override
    public void endExport() {
    }

    private String defineSeparator(TranslatablePropertyValueInfo translatablePropertyValueInfo) {
        if (translatablePropertyValueInfo.getId().equals(FormatterProperties.SEPARATOR_SEMICOLON.getKey())) {
            return SEMICOLON_SEPARATOR;
        }
        return COMMA_SEPARATOR;
    }
}
