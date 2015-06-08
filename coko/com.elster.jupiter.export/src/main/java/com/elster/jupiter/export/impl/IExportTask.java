package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

interface IExportTask extends ExportTask, HasAuditInfo {
    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    List<DataExportProperty> getDataExportProperties();

    void setReadingTypeDataSelector(ReadingTypeDataSelectorImpl readingTypeDataSelector);

    boolean hasDefaultSelector();

    Destination getCompositeDestination();
}
