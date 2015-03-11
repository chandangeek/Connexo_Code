package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;

import java.time.Instant;


public class DataValidationTaskBuilderImpl implements DataValidationTaskBuilder {

    private final DataModel dataModel;

    private String name;
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately ;
    private EndDeviceGroup endDeviceGroup;


    public DataValidationTaskBuilderImpl(DataModel dataModel){
        this.dataModel = dataModel;
        this.scheduleImmediately = false;
    }


    @Override
    public DataValidationTaskBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DataValidationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }

    @Override
    public DataValidationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public DataValidationTaskBuilder scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;

    }

    @Override
    public DataValidationTaskBuilder setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public DataValidationTask build() {
        DataValidationTaskImpl task = DataValidationTaskImpl.from(dataModel, name);
        task.setScheduleImmediately(scheduleImmediately);
        task.setScheduleExpression(scheduleExpression);
        task.setEndDeviceGroup(endDeviceGroup);
        return task;
    }
}
