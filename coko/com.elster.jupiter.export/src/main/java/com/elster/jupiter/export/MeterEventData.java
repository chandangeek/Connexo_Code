/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

public class MeterEventData extends AbstractExportData<MeterReading> {

    public MeterEventData(MeterReading data, StructureMarker structureMarker) {
        super(data, structureMarker);
    }

    public MeterReading getMeterReading() {
        return getData();
    }
}
