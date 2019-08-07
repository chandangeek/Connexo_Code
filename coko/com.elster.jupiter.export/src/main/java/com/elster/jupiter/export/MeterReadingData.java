/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;
import com.google.common.collect.Range;

import java.time.Instant;

public class MeterReadingData extends AbstractExportData<MeterReading> {

    private final ReadingTypeDataExportItem item;
    private final MeterReadingValidationData validationData;

    private final boolean customSelector;
    private final Range<Instant> exportInterval;

    public MeterReadingData(ReadingTypeDataExportItem item, MeterReading data, MeterReadingValidationData validationData, StructureMarker structureMarker) {
        super(data, structureMarker);
        this.item = item;
        this.validationData = validationData;
        this.customSelector = false;
        this.exportInterval = null;
    }

    public MeterReadingData(ReadingTypeDataExportItem item, MeterReading data, MeterReadingValidationData validationData,
                            StructureMarker structureMarker, boolean customSelector, Range<Instant> exportInterval) {
        super(data, structureMarker);
        this.item = item;
        this.validationData = validationData;
        this.customSelector = customSelector;
        this.exportInterval = exportInterval;
    }

    public MeterReading getMeterReading() {
        return getData();
    }

    public MeterReadingValidationData getValidationData() {
        return validationData;
    }

    public ReadingTypeDataExportItem getItem() {
        return item;
    }

    public Range<Instant> getExportInterval() {
        return exportInterval;
    }

    public boolean isCustomSelector() {
        return customSelector;
    }
}
