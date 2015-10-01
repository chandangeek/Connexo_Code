package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

public class MeterEventData extends AbstractExportData<MeterReading> {
    public MeterEventData(MeterReading data, StructureMarker structureMarker) {
        super(data, structureMarker);
    }
}
