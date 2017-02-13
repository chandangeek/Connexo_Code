package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 1/10/2015
 * Time: 14:54
 */
public class EstimationTaskBuilder extends NamedBuilder<EstimationTask, EstimationTaskBuilder> {
    private final EstimationService estimationService;
    private final TaskService taskService;
    private final Clock clock;

    private Instant nextExecution;
    private EndDeviceGroup deviceGroup;
    private ScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(7, 0, 0).build();

    @Inject
    public EstimationTaskBuilder(EstimationService estimationService, TaskService taskService, Clock clock) {
        super(EstimationTaskBuilder.class);
        this.estimationService = estimationService;
        this.taskService = taskService;
        this.clock = clock;
    }

    public EstimationTaskBuilder withEndDeviceGroup(EndDeviceGroup group) {
        this.deviceGroup = group;
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
        return this.taskService.getRecurrentTask(getName()).flatMap(this::findByRecurrentTask);
    }

    private Optional<EstimationTask> findByRecurrentTask(RecurrentTask recurrentTask) {
        return this.estimationService.getEstimationTaskQuery()
                .select(Where.where("recurrentTask").isEqualTo(recurrentTask))
                .stream()
                .map(EstimationTask.class::cast)
                .findFirst();
    }

    @Override
    public EstimationTask create() {
        Log.write(this);
        com.elster.jupiter.estimation.EstimationTaskBuilder taskBuilder = estimationService.newBuilder();
        taskBuilder.setName(getName());
        taskBuilder.setLogLevel(Level.WARNING.intValue());
        taskBuilder.setQualityCodeSystem(QualityCodeSystem.MDC);
        taskBuilder.setEndDeviceGroup(deviceGroup);
        taskBuilder.setScheduleExpression(scheduleExpression);
        taskBuilder.setNextExecution(nextExecution);

        EstimationTask task = taskBuilder.create();
        applyPostBuilders(task);
        return task;
    }
}
