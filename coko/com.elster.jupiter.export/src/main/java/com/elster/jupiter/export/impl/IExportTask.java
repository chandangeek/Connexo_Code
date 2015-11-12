package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Optional;

interface IExportTask extends ExportTask, HasAuditInfo {
    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    List<DataExportProperty> getDataExportProperties();

    void setReadingTypeDataSelector(StandardDataSelectorImpl readingTypeDataSelector);

    void setEventDataSelector(StandardDataSelectorImpl eventDataSelector);

    boolean hasDefaultSelector();

    Destination getCompositeDestination();

    Optional<IStandardDataSelector> getReadingTypeDataSelector();
}
