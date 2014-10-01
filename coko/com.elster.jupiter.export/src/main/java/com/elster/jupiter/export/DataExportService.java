package com.elster.jupiter.export;

public interface DataExportService {

    DataFormatterFactory getDataFormatterFactory(String name);

//    DataExportTask createDataExportTask(String dataFormatter, )
}
