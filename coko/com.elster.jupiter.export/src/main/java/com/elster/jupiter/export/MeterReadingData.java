package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

public class MeterReadingData extends AbstractExportData<MeterReading> {

    private final ReadingTypeDataExportItem item;

    public MeterReadingData(ReadingTypeDataExportItem item, MeterReading data, StructureMarker structureMarker) {
        super(data, structureMarker);
        this.item = item;
    }

    public MeterReading getMeterReading() {
        return getData(MeterReading.class);
    }

    public ReadingTypeDataExportItem getItem() {
        return item;
    }
}
