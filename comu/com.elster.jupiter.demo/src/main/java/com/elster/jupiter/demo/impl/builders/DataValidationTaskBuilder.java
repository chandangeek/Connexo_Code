/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;

import com.google.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.logging.Level;

public class DataValidationTaskBuilder extends NamedBuilder<DataValidationTask, DataValidationTaskBuilder> {
    private final ValidationService validationService;
    private final Clock clock;
    private QualityCodeSystem qualityCodeSystem = QualityCodeSystem.MDC;
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose purpose;
    private Instant nextExecution;
    private ScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(6, 0, 0).build();

    @Inject
    public DataValidationTaskBuilder(ValidationService validationService, Clock clock) {
        super(DataValidationTaskBuilder.class);
        this.validationService = validationService;
        this.clock = clock;
    }

    public DataValidationTaskBuilder withQualityCodeSystem(QualityCodeSystem qualityCodeSystem) {
        this.qualityCodeSystem = qualityCodeSystem;
        return this;
    }

    public DataValidationTaskBuilder withEndDeviceGroup(EndDeviceGroup group) {
        this.endDeviceGroup = group;
        return this;
    }

    public DataValidationTaskBuilder withUsagePointGroup(UsagePointGroup group) {
        this.usagePointGroup = group;
        return this;
    }

    public DataValidationTaskBuilder withMetrologyPurpose(MetrologyPurpose purpose) {
        this.purpose = purpose;
        return this;
    }

    public DataValidationTaskBuilder withScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    public DataValidationTaskBuilder withNextExecution() {
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        Optional<ZonedDateTime> nextOccurrence = scheduleExpression.nextOccurrence(now);
        this.nextExecution = nextOccurrence.map(ZonedDateTime::toInstant).orElse(null);
        return this;
    }

    @Override
    public Optional<DataValidationTask> find() {
        return validationService.findValidationTaskByName(qualityCodeSystem, getName());
    }

    @Override
    public DataValidationTask create() {
        Log.write(this);
        com.elster.jupiter.validation.DataValidationTaskBuilder taskBuilder = validationService.newTaskBuilder();
        taskBuilder.setName(getName());
        taskBuilder.setLogLevel(Level.WARNING.intValue());
        taskBuilder.setQualityCodeSystem(qualityCodeSystem);
        if (qualityCodeSystem == QualityCodeSystem.MDC) {
            taskBuilder.setEndDeviceGroup(endDeviceGroup);
        } else if (qualityCodeSystem == QualityCodeSystem.MDM) {
            taskBuilder.setUsagePointGroup(usagePointGroup);
            taskBuilder.setMetrologyPurpose(purpose);
        } else {
            throw new UnableToCreate("Unsupported quality code system in data validation task builder.");
        }
        taskBuilder.setScheduleExpression(scheduleExpression);
        taskBuilder.setNextExecution(nextExecution);

        DataValidationTask task = taskBuilder.create();
        applyPostBuilders(task);

        return task;
    }
}
