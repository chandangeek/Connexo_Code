/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

public class MeterReadingData extends AbstractExportData<MeterReading> {

    private final ReadingTypeDataExportItem item;
    private final MeterReadingValidationData validationData;

    public MeterReadingData(ReadingTypeDataExportItem item, MeterReading data, MeterReadingValidationData validationData, StructureMarker structureMarker) {
        super(data, structureMarker);
        this.item = item;
        this.validationData = validationData;
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
}
