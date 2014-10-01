package com.elster.jupiter.export;

public interface DataExportService {

    DataFormatterFactory getDataFormatterFactory(String name);

    DataExportTaskBuilder newBuilder();
}
