/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;

import java.time.Instant;

public class DataValidationTaskBuilderImpl implements DataValidationTaskBuilder {

    private final DataModel dataModel;

    private String name;
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose metrologyPurpose;
    private ValidationService dataValidationService;
    private QualityCodeSystem qualityCodeSystem;
    private int logLevel;

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
    public DataValidationTaskBuilder setQualityCodeSystem(QualityCodeSystem qualityCodeSystem) {
        this.qualityCodeSystem = qualityCodeSystem;
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
    public DataValidationTaskBuilder setMetrologyPurpose(MetrologyPurpose metrologyPurpose) {
        this.metrologyPurpose = metrologyPurpose;
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
    public DataValidationTaskBuilder setLogLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public DataValidationTask create() {
        DataValidationTaskImpl task = DataValidationTaskImpl.from(dataModel, name, nextExecution, qualityCodeSystem, logLevel);
        task.setScheduleImmediately(scheduleImmediately);
        task.setScheduleExpression(scheduleExpression);
        task.setEndDeviceGroup(endDeviceGroup);
        task.setUsagePointGroup(usagePointGroup);
        task.setMetrologyPurpose(metrologyPurpose);
        task.doSave();
        return task;
    }
}
