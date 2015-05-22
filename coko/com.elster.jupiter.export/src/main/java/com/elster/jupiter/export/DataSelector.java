package com.elster.jupiter.export;

import java.util.stream.Stream;

public interface DataSelector {

    Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence);
}
