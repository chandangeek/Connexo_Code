package com.elster.jupiter.export;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Optional;

public interface DataExportService {

    String COMPONENTNAME = "DES";

    Optional<DataProcessorFactory> getDataFormatterFactory(String name);

    DataExportTaskBuilder newBuilder();

    Optional<? extends ReadingTypeDataExportTask> findExportTask(long id);

    Query<? extends ReadingTypeDataExportTask> getReadingTypeDataExportTaskQuery();

    List<PropertySpec<?>> getPropertiesSpecsForProcessor(String name);

    List<DataProcessorFactory> getAvailableProcessors();

}
