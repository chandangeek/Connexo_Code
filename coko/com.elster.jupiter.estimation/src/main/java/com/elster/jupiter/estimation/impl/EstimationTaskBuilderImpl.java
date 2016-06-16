package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

class EstimationTaskBuilderImpl implements EstimationTaskBuilder {

    private final DataModel dataModel;

    private ScheduleExpression scheduleExpression;
    private RelativePeriod period;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private String name;
    private EndDeviceGroup endDeviceGroup;
    private String applicationName;

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
    public EstimationTaskBuilder setApplication(String application) {
        this.applicationName = application;
        return this;
    }

    @Override
    public EstimationTaskBuilder scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public EstimationTask create() {
        IEstimationTask task = EstimationTaskImpl.from(dataModel, name, endDeviceGroup, scheduleExpression, nextExecution, applicationName);
        task.setScheduleImmediately(scheduleImmediately);
        if (period != null) {
            task.setPeriod(period);
        }
        task.doSave();
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

    @Override
    public EstimationTaskBuilder setPeriod(RelativePeriod relativePeriod) {
        this.period = relativePeriod;
        return this;
    }
}
