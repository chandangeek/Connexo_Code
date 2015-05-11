package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;

interface IReadingTypeExportTask extends ReadingTypeDataExportTask, IExportTask {

    void setScheduleImmediately(boolean scheduleImmediately);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportUpdate(boolean exportUpdate);

    IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType);

    List<? extends IReadingTypeDataExportItem> getExportItems();

    List<DataExportProperty> getDataExportProperties();


}
