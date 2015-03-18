package com.elster.jupiter.validation.impl;


import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.DataValidationTask;

import java.util.List;

interface IDataValidationTask extends DataValidationTask, HasAuditInfo {
/*
    PropertySpec getPropertySpec(String name);

    String getDisplayName(String name);*/

  /*  void setScheduleImmediately(boolean scheduleImmediately);

    void setValidatedDataOption(ValidatedDataOption validatedDataOption);

    void setExportContinuousData(boolean exportContinuousData);

    void setExportUpdate(boolean exportUpdate);

    IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType);

    List<? extends IReadingTypeDataExportItem> getExportItems();

    List<DataExportProperty> getDataExportProperties();*/


}
