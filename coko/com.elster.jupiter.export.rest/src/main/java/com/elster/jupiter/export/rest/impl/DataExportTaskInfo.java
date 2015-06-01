package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement
public class DataExportTaskInfo {

    public long id = 0;
    public boolean active = true;
    public String name = "name";
    public ProcessorInfo dataProcessor;
    public PeriodicalExpressionInfo schedule;
    public RelativePeriodInfo exportperiod;
    public RelativePeriodInfo updatePeriod;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public boolean exportUpdate;
    public boolean exportContinuousData;
    public ValidatedDataOption validatedDataOption;
    public MeterGroupInfo deviceGroup;
    public DataExportTaskHistoryInfo lastExportOccurence;
    public Long nextRun;
    public Long lastRun;


    public DataExportTaskInfo(ReadingTypeDataExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        doPopulate(dataExportTask, thesaurus, timeService);
        for (ReadingType readingType : dataExportTask.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
        if (Never.NEVER.equals(dataExportTask.getScheduleExpression())) {
            schedule = null;
        } else {
            ScheduleExpression scheduleExpression = dataExportTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        properties = propertyUtils.convertPropertySpecsToPropertyInfos(dataExportTask.getPropertySpecs(), dataExportTask.getProperties());
        lastExportOccurence = dataExportTask.getLastOccurrence().map(oc -> new DataExportTaskHistoryInfo(oc, thesaurus, timeService, propertyUtils)).orElse(null);

    }

    public void populate(ReadingTypeDataExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService) {
        doPopulate(dataExportTask, thesaurus, timeService);
    }

    private void doPopulate(ReadingTypeDataExportTask dataExportTask, Thesaurus thesaurus, TimeService timeService) {
        id = dataExportTask.getId();
        name = dataExportTask.getName();

        deviceGroup = new MeterGroupInfo(dataExportTask.getEndDeviceGroup());
        active = dataExportTask.isActive();
        exportperiod = new RelativePeriodInfo(dataExportTask.getExportPeriod(), thesaurus);
        exportContinuousData = dataExportTask.getStrategy().isExportContinuousData();
        exportUpdate = dataExportTask.getStrategy().isExportUpdate();
        Optional<RelativePeriod> dataExportTaskUpdatePeriod = dataExportTask.getUpdatePeriod();
        if (dataExportTaskUpdatePeriod.isPresent()) {
            updatePeriod = new RelativePeriodInfo(dataExportTaskUpdatePeriod.get(), thesaurus);
        }
        validatedDataOption = dataExportTask.getStrategy().getValidatedDataOption();

        String dataFormatter = dataExportTask.getDataFormatter();
        dataProcessor = new ProcessorInfo(dataFormatter, thesaurus.getStringBeyondComponent(dataFormatter, dataFormatter), Collections.<PropertyInfo>emptyList()) ;

        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        Optional<Instant> lastRunOptional = dataExportTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
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
