package com.elster.jupiter.validation.impl;

import java.time.Instant;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;

public class DataValidationTaskBuilderImpl implements DataValidationTaskBuilder {

    private final DataModel dataModel;

    private String name;
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private ValidationService dataValidationService;

    public DataValidationTaskBuilderImpl(DataModel dataModel, ValidationService dataValidationService) {
        this.dataModel = dataModel;
        this.dataValidationService = dataValidationService;
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
    public DataValidationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
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
        DataValidationTaskImpl task = DataValidationTaskImpl.from(dataModel, name, nextExecution, dataValidationService);
        task.setScheduleImmediately(scheduleImmediately);
        task.setScheduleExpression(scheduleExpression);
        task.setEndDeviceGroup(endDeviceGroup);
        task.setUsagePointGroup(usagePointGroup);
        return task;
    }
}
