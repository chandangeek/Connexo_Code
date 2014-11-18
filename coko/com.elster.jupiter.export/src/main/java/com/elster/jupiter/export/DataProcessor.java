package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public interface DataProcessor {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    Optional<Instant> processData(MeterReading data);

    void processUpdatedData(MeterReading data);

    void endItem();

    void endExport();

    List<PropertySpec<?>> getPropertySpecs();
}
