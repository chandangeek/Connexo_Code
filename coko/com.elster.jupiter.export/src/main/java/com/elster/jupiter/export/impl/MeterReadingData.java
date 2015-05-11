package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.AbstractExportData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.readings.MeterReading;

public class MeterReadingData extends AbstractExportData<MeterReading> {

    public MeterReadingData(MeterReading data, StructureMarker structureMarker) {
        super(data, structureMarker);
    }

    public MeterReading getMeterReading() {
        return getData(MeterReading.class);
    }
}
