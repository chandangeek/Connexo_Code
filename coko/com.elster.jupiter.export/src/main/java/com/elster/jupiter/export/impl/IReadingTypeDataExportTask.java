package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface IReadingTypeDataExportTask extends ReadingTypeDataExportTask {

    PropertySpec<?> getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportUpdate(boolean exportUpdate);

    IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType);

    List<? extends IReadingTypeDataExportItem> getExportItems();

    List<DataExportProperty> getDataExportProperties();


}
