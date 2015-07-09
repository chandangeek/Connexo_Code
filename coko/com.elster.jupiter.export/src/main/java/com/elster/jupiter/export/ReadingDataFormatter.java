package com.elster.jupiter.export;

public interface ReadingDataFormatter extends DataFormatter {
    void startItem(ReadingTypeDataExportItem item);

    void endItem(ReadingTypeDataExportItem item);
}
