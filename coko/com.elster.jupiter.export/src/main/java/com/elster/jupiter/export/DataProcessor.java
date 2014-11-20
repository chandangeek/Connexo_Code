package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.metering.readings.MeterReading;

import java.time.Instant;
import java.util.logging.Logger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public interface DataProcessor {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    Optional<Instant> processData(MeterReading data);

    Optional<Instant> processUpdatedData(MeterReading updatedData);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();
}
