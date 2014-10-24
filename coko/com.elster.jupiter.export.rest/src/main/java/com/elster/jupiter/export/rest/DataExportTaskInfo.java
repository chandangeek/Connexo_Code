package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataExportTaskInfo {

    public long id = 0;
    public boolean active = true;
    public String name = "name";
    public String dataProcessor = "dataProcessor"; //dataprocessor name
    public TemporalExpressionInfo schedule = TemporalExpressionInfo.from(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(6)));
    public RelativePeriodInfo exportperiod;
    public RelativePeriodInfo updatePeriod;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public boolean exportUpdate;
    public boolean exportContinuousData;
    public ValidatedDataOption validatedDataOption = ValidatedDataOption.INCLUDE_ALL;
    public long lastRun = Instant.now().toEpochMilli();
    public long endDeviceGroupId;


    public DataExportTaskInfo(ReadingTypeDataExportTask dataExportTask) {
        id = dataExportTask.getId();
        active = dataExportTask.isActive();
        dataProcessor = dataExportTask.getDataFormatter();
        name = dataExportTask.getName();
        dataExportTask.getExportPeriod();


        properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(dataExportTask.getPropertySpecs(), dataExportTask.getProperties());
        for (ReadingType readingType : dataExportTask.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    public DataExportTaskInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((DataExportTaskInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
