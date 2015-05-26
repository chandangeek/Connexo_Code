package com.elster.jupiter.export;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.properties.PropertySpec;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DataExportService {

    String COMPONENTNAME = "DES";
    String STANDARD_DATA_SELECTOR = "Standard Data Selector";

    Optional<DataProcessorFactory> getDataFormatterFactory(String name);

    DataExportTaskBuilder newBuilder();

    Optional<? extends ReadingTypeDataExportTask> findExportTask(long id);

    Query<? extends ReadingTypeDataExportTask> getReadingTypeDataExportTaskQuery();

    List<PropertySpec> getPropertiesSpecsForProcessor(String name);

    List<DataSelectorFactory> getAvailableSelectors();

    List<DataProcessorFactory> getAvailableProcessors();

    List<? extends ReadingTypeDataExportTask> findReadingTypeDataExportTasks();

    Optional<? extends DataExportOccurrence> findDataExportOccurrence(ReadingTypeDataExportTask task, Instant triggerTime);

    void setExportDirectory(AppServer appServer, Path path);

    void removeExportDirectory(AppServer appServer);

    Optional<Path> getExportDirectory(AppServer appServer);

    Map<AppServer, Path> getAllExportDirecties();
}
