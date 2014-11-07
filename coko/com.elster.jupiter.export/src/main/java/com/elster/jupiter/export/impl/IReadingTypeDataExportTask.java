package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.logging.Logger;

interface IReadingTypeDataExportTask extends ReadingTypeDataExportTask {
    void execute(DataExportOccurrence occurrence, Logger logger);

    PropertySpec<?> getPropertySpec(String name);

    String getDisplayName(String name);

    void setScheduleImmediately(boolean scheduleImmediately);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportUpdate(boolean exportUpdate);

    IReadingTypeDataExportItem addExportItem(Meter meter, String readingTypeMRId);
}
