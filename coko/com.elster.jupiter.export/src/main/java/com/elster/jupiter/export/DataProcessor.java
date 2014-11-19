package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;

import java.time.Instant;
import java.util.logging.Logger;

public interface DataProcessor {

    void startExport(DataExportOccurrence dataExportOccurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    Instant processData(MeterReading data);

    Instant processUpdatedData(MeterReading updatedData);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();
}
