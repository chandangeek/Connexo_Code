/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

class StandardCsvEventDataFormatter extends AbstractCsvEventDataFormatter {

    StandardCsvEventDataFormatter(DataExportService dataExportService) {
        super(dataExportService);
    }

    private StandardCsvEventDataFormatter init(TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag) {
        super.initialize(translatablePropertyValueInfo, tag);
        return this;
    }

    static StandardCsvEventDataFormatter from(DataExportService dataExportService, TranslatablePropertyValueInfo translatablePropertyValueInfo, String tag) {
        return new StandardCsvEventDataFormatter(dataExportService).init(translatablePropertyValueInfo, tag);
    }

    @Override
    protected String formatPayload(EndDeviceEvent endDeviceEvent, StructureMarker structureMarker) {
        ZonedDateTime eventTime = ZonedDateTime.ofInstant(endDeviceEvent.getCreatedDateTime(), ZoneId.systemDefault());
        StringJoiner joiner = new StringJoiner(separator, "", "\n")
                .add(DEFAULT_DATE_TIME_FORMAT.format(eventTime))
                .add(endDeviceEvent.getEventTypeCode());
        // adding list of device identifiers; see com.elster.jupiter.export.impl.EventSelector.buildStructureMarker
        structureMarker.getStructurePath().forEach(joiner::add);
        return joiner.toString();
    }

}
