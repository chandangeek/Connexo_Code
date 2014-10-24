package com.elster.jupiter.export;

import com.elster.jupiter.domain.util.Query;

import java.util.Optional;

public interface DataExportService {

    String COMPONENTNAME = "DES";

    Optional<DataProcessorFactory> getDataFormatterFactory(String name);

    DataExportTaskBuilder newBuilder();

    Optional<? extends ReadingTypeDataExportTask> findExportTask(long id);

    Query<? extends ReadingTypeDataExportTask> getReadingTypeDataExportTaskQuery();

}
