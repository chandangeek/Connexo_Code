package com.elster.jupiter.export;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.logging.Logger;

public interface DataProcessor {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    void processData(MeterReading data);

    void processUpdatedData(MeterReading data);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();

    List<PropertySpec<?>> getPropertySpecs();
}
