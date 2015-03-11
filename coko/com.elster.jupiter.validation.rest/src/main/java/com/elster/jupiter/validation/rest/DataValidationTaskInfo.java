package com.elster.jupiter.validation.rest;



import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
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
    public MeterGroupInfo endDeviceGroup;
    public Instant lastRun;
    public Instant nextRun;
    public PeriodicalExpressionInfo schedule;

    public DataValidationTaskInfo(DataValidationTask dataValidationTask) {//), Thesaurus thesaurus, TimeService timeService) {

        id = dataValidationTask.getId();
        name = dataValidationTask.getName();
        endDeviceGroup = new MeterGroupInfo();

        endDeviceGroup.id = dataValidationTask.getEndDeviceGroup().getId();
        endDeviceGroup.name = dataValidationTask.getEndDeviceGroup().getName();

        //endDeviceGroup.setName("group1");
        lastRun = Instant.now();
        //endDeviceGroup.se
        /*
        try {
            endDeviceGroup = dataValidationTask.getEndDeviceGroup();
        }
        catch(Exception ex)
        {

        }
        */
        nextRun = dataValidationTask.getNextExecution();
    }


    public long getId()
    {
        return this.id;
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
