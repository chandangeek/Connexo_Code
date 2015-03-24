package com.elster.jupiter.validation.rest;



import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.DataValidationTask;
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
public class DataValidationTaskInfo {

    public long id = 0;
    public String name = "blank_name";
    public MeterGroupInfo deviceGroup;
    public PeriodicalExpressionInfo schedule;
    public DataValidationTaskHistoryInfo lastValidationOccurence;
    public Long nextRun;
    public Long lastRun;
    

    public DataValidationTaskInfo(DataValidationTask dataValidationTask, Thesaurus thesaurus) {
        populate(dataValidationTask);
        lastValidationOccurence = dataValidationTask.getLastOccurrence().map(oc -> new DataValidationTaskHistoryInfo(oc, thesaurus)).orElse(null);
    }


    public void populate(DataValidationTask dataValidationTask) {
        doPopulate(dataValidationTask);
    }

    private void doPopulate(DataValidationTask dataValidationTask) {

        id = dataValidationTask.getId();
        name = dataValidationTask.getName();
        deviceGroup = new MeterGroupInfo(dataValidationTask.getEndDeviceGroup());

        if (Never.NEVER.equals(dataValidationTask.getScheduleExpression())) {
            schedule = null;
        } else {
            ScheduleExpression scheduleExpression = dataValidationTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }

        Instant nextExecution = dataValidationTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }

        Optional<Instant> lastRunOptional = dataValidationTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }


    }

    public long getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public DataValidationTaskInfo() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((DataValidationTaskInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
