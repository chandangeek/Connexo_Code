/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
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

abstract class AbstractCsvEventDataFormatter implements StandardFormatter {

    protected static final String SEMICOLON_SEPARATOR = ";";
    protected static final String COMMA_SEPARATOR = ",";
    protected final DataExportService dataExportService;
    protected String separator;
    protected String tag;

    AbstractCsvEventDataFormatter(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    protected void initialize(TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag) {
        this.separator = defineSeparator(translatablePropertyValueInfo);
        this.tag = tag;
    }

    @Override
    public void startExport(DataExportOccurrence occurrence, Logger logger) {
    }

    @Override
    public FormattedData processData(Stream<ExportData> data) {
        StructureMarker rootStructureMarker = dataExportService.forRoot(tag);
        return SimpleFormattedData.of(processToLines(data, rootStructureMarker));
    }

    protected List<FormattedExportData> processToLines(Stream<ExportData> data, StructureMarker rootStructureMarker) {
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

    abstract protected String formatPayload(EndDeviceEvent endDeviceEvent, StructureMarker structureMarker);

    @Override
    public void endExport() {
    }

    protected String defineSeparator(TranslatablePropertyValueInfo translatablePropertyValueInfo) {
        if (translatablePropertyValueInfo.getId().equals(FormatterProperties.SEPARATOR_SEMICOLON.getKey())) {
            return SEMICOLON_SEPARATOR;
        }
        return COMMA_SEPARATOR;
    }
}
