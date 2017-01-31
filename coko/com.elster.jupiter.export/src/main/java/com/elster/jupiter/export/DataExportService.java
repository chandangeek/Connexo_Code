/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface DataExportService {

    String COMPONENTNAME = "DES";

    String STANDARD_READINGTYPE_DATA_SELECTOR = "Standard Data Selector";
    String STANDARD_USAGE_POINT_DATA_SELECTOR = "Standard Usage Point Data Selector";
    String STANDARD_EVENT_DATA_SELECTOR = "Standard Event Data Selector";

    String DATA_TYPE_PROPERTY = "dataType";

    String STANDARD_READING_DATA_TYPE = "standardReadingDataType";
    String STANDARD_EVENT_DATA_TYPE = "standardEventDataType";
    String STANDARD_USAGE_POINT_DATA_TYPE = "standardUsagePointDataType";

    DataExportTaskBuilder newBuilder();

    ExportTaskFinder findExportTasks();

    Optional<? extends ExportTask> findExportTask(long id);

    Optional<? extends ExportTask> findAndLockExportTask(long id, long version);

    Optional<? extends ExportTask> getReadingTypeDataExportTaskByName(String name);

    List<DataFormatterFactory> getAvailableFormatters();

    Optional<DataFormatterFactory> getDataFormatterFactory(String name);

    List<PropertySpec> getPropertiesSpecsForFormatter(String name);

    List<DataSelectorFactory> getAvailableSelectors();

    List<DataFormatterFactory> formatterFactoriesMatching(DataSelectorFactory selectorFactory);

    Optional<DataSelectorFactory> getDataSelectorFactory(String dataSelector);

    List<PropertySpec> getPropertiesSpecsForDataSelector(String name);

    List<? extends ExportTask> findReadingTypeDataExportTasks();

    Optional<? extends DataExportOccurrence> findDataExportOccurrence(ExportTask task, Instant triggerTime);

    void setExportDirectory(AppServer appServer, Path path);

    void removeExportDirectory(AppServer appServer);

    Optional<Path> getExportDirectory(AppServer appServer);

    Map<AppServer, Path> getAllExportDirecties();

    StructureMarker forRoot(String root);

}
