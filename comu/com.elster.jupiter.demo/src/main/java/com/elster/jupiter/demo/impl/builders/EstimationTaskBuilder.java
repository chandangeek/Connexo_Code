/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationTaskBuilder extends NamedBuilder<EstimationTask, EstimationTaskBuilder> {
    private final EstimationService estimationService;
    private final TimeService timeService;
    private final TaskService taskService;
    private final Clock clock;

    private QualityCodeSystem qualityCodeSystem = QualityCodeSystem.MDC;
    private EndDeviceGroup deviceGroup;
    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose purpose;
    private Instant nextExecution;
    private ScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(7, 0, 0).build();

    @Inject
    public EstimationTaskBuilder(EstimationService estimationService, TimeService timeService, TaskService taskService, Clock clock) {
        super(EstimationTaskBuilder.class);
        this.estimationService = estimationService;
        this.timeService = timeService;
        this.taskService = taskService;
        this.clock = clock;
    }

    public EstimationTaskBuilder withQualityCodeSystem(QualityCodeSystem qualityCodeSystem) {
        this.qualityCodeSystem = qualityCodeSystem;
        return this;
    }

    public EstimationTaskBuilder withEndDeviceGroup(EndDeviceGroup group) {
        this.deviceGroup = group;
        return this;
    }

    public EstimationTaskBuilder withUsagePointGroup(UsagePointGroup group) {
        this.usagePointGroup = group;
        return this;
    }

    public EstimationTaskBuilder withMetrologyPurpose(MetrologyPurpose purpose) {
        this.purpose = purpose;
        return this;
    }

    public EstimationTaskBuilder withScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    public EstimationTaskBuilder withNextExecution() {
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        Optional<ZonedDateTime> nextOccurrence = scheduleExpression.nextOccurrence(now);
        this.nextExecution = nextOccurrence.map(ZonedDateTime::toInstant).orElse(null);
        return this;
    }

    @Override
    public Optional<EstimationTask> find() {
        return this.estimationService.getEstimationTaskQuery()
                .select(where("recurrentTask").in(findRecurrentTasksByName())
                        .and(where("qualityCodeSystem").isEqualTo(qualityCodeSystem)))
                .stream()
                .findFirst()
                .map(EstimationTask.class::cast);
    }

    private List<? extends RecurrentTask> findRecurrentTasksByName() {
        return taskService.getTaskQuery().select(where("name").isEqualTo(getName()));
    }

    @Override
    public EstimationTask create() {
        Log.write(this);
        com.elster.jupiter.estimation.EstimationTaskBuilder taskBuilder = estimationService.newBuilder();
        taskBuilder.setName(getName());
        taskBuilder.setLogLevel(Level.WARNING.intValue());
        taskBuilder.setQualityCodeSystem(qualityCodeSystem);
        if (qualityCodeSystem == QualityCodeSystem.MDC) {
            taskBuilder.setEndDeviceGroup(deviceGroup);
        } else if (qualityCodeSystem == QualityCodeSystem.MDM) {
            taskBuilder.setUsagePointGroup(usagePointGroup);
            taskBuilder.setMetrologyPurpose(purpose);
        } else {
            throw new UnableToCreate("Unsupported quality code system in estimation task builder.");
        }
        taskBuilder.setScheduleExpression(scheduleExpression);
        taskBuilder.setNextExecution(nextExecution);
        taskBuilder.setRevalidate(false);
        taskBuilder.setPeriod(timeService.findRelativePeriodByName("Last 7 days").get());

        EstimationTask task = taskBuilder.create();
        applyPostBuilders(task);
        return task;
    }
}
