package com.elster.jupiter.export;

import java.util.logging.Logger;

public interface DataFormatter {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    void startItem(ReadingTypeDataExportItem item);

    FormattedData processData(ExportData data);

    void endItem(ReadingTypeDataExportItem item);

    void endExport();
}
