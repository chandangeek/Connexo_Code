package com.elster.jupiter.export;

import java.util.logging.Logger;
import java.util.stream.Stream;

public interface DataFormatter {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    FormattedData processData(Stream<ExportData> data);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();
}
