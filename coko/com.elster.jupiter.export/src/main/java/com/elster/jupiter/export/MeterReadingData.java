/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

import java.time.Instant;
import java.util.Map;

public class MeterReadingData extends AbstractExportData<MeterReading> {

    private final ReadingTypeDataExportItem item;
    private final MeterReadingValidationData validationData;
    private final Map<Instant, String> readingStatuses;

    public MeterReadingData(ReadingTypeDataExportItem item, MeterReading data, MeterReadingValidationData validationData, Map<Instant, String> readingStatuses, StructureMarker structureMarker) {
        super(data, structureMarker);
        this.item = item;
        this.validationData = validationData;
        this.readingStatuses = readingStatuses;
    }

    public MeterReading getMeterReading() {
        return getData();
    }

    public MeterReadingValidationData getValidationData() {
        return validationData;
    }

    public Map<Instant, String> getReadingStatuses() {
        return readingStatuses;
    }

    public ReadingTypeDataExportItem getItem() {
        return item;
    }

}
