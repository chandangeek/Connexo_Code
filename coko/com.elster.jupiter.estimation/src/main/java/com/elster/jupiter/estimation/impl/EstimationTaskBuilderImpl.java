package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
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
    private UsagePointGroup usagePointGroup;
    private QualityCodeSystem qualityCodeSystem;
    private int logLevel;

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
    public EstimationTaskBuilder setQualityCodeSystem(QualityCodeSystem qualityCodeSystem) {
        this.qualityCodeSystem = qualityCodeSystem;
        return this;
    }

    @Override
    public EstimationTaskBuilder scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public EstimationTaskBuilder setLogLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public EstimationTask create() {
        IEstimationTask task = EstimationTaskImpl.from(dataModel, name, endDeviceGroup, usagePointGroup, scheduleExpression, nextExecution, qualityCodeSystem, logLevel);
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
    public EstimationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
        return this;
    }

    @Override
    public EstimationTaskBuilder setPeriod(RelativePeriod relativePeriod) {
        this.period = relativePeriod;
        return this;
    }
}
