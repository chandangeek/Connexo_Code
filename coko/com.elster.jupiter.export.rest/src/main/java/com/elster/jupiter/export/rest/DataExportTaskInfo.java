package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public DeviceGroupInfo deviceGroup;
    public LastExportOccurenceInfo lastExportOccurence;
    public long nextRun;


    public DataExportTaskInfo(ReadingTypeDataExportTask dataExportTask) {
        id = dataExportTask.getId();
        name = dataExportTask.getName();

        deviceGroup = new DeviceGroupInfo(dataExportTask.getEndDeviceGroup());
        for (ReadingType readingType : dataExportTask.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }

        active = dataExportTask.isActive();
        //TODO schedule !!
        //schedule = dataExportTask.getSchedule() ?

        exportperiod = new RelativePeriodInfo(dataExportTask.getExportPeriod());
        exportContinuousData = dataExportTask.getStrategy().isExportContinuousData();
        exportUpdate = dataExportTask.getStrategy().isExportUpdate();
        Optional<RelativePeriod> dataExportTaskUpdatePeriod = dataExportTask.getUpdatePeriod();
        if (dataExportTaskUpdatePeriod.isPresent()) {
            updatePeriod = new RelativePeriodInfo(dataExportTaskUpdatePeriod.get());
        }
        validatedDataOption = dataExportTask.getStrategy().getValidatedDataOption();

        dataProcessor = dataExportTask.getDataFormatter();
        properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(dataExportTask.getPropertySpecs(), dataExportTask.getProperties());

        //todo last occurence
        // lastExportOccurence = new LastExportOccurenceInfo(dataExportTask.getLastOccurence());
        nextRun = dataExportTask.getNextExecution().toEpochMilli();


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
