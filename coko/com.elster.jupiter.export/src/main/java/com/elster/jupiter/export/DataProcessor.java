package com.elster.jupiter.export;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

public interface DataProcessor {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    Optional<Instant> processData(ExportData data);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();
}
