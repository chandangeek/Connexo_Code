package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

public class EstimationTaskBuilderImpl implements EstimationTaskBuilder {

    private final DataModel dataModel;

    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private String name;
    private EndDeviceGroup endDeviceGroup;

    public EstimationTaskBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public EstimationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public EstimationTaskBuilder setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public EstimationTaskBuilder scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public EstimationTask build() {
        IEstimationTask task = EstimationTaskImpl.from(dataModel, name, endDeviceGroup, scheduleExpression, nextExecution);
        task.setScheduleImmediately(scheduleImmediately);
        return task;
    }

    @Override
    public EstimationTaskBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public EstimationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }
}
