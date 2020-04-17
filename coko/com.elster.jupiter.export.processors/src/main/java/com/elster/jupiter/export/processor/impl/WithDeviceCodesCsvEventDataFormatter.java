/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

class WithDeviceCodesCsvEventDataFormatter extends AbstractCsvEventDataFormatter implements StandardFormatter {

    WithDeviceCodesCsvEventDataFormatter(DataExportService dataExportService) {
        super(dataExportService);
    }

    private WithDeviceCodesCsvEventDataFormatter init(TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag) {
        super.initialize(translatablePropertyValueInfo, tag);
        return this;
    }

    static WithDeviceCodesCsvEventDataFormatter from(DataExportService dataExportService, TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag) {
        return new WithDeviceCodesCsvEventDataFormatter(dataExportService).init(translatablePropertyValueInfo, tag);
    }

    @Override
    protected String formatPayload(EndDeviceEvent endDeviceEvent, StructureMarker structureMarker) {
        ZonedDateTime eventTime = ZonedDateTime.ofInstant(endDeviceEvent.getCreatedDateTime(), ZoneId.systemDefault());
        StringJoiner joiner = new StringJoiner(separator, "", "\n")
                .add(DEFAULT_DATE_TIME_FORMAT.format(eventTime))
                .add(endDeviceEvent.getEventTypeCode())
                .add(endDeviceEvent.getType());
        // adding list of device identifiers; see com.elster.jupiter.export.impl.EventSelector.buildStructureMarker
        structureMarker.getStructurePath().forEach(joiner::add);
        return joiner.toString();
    }

    @Override
    public void endExport() {
    }
}
