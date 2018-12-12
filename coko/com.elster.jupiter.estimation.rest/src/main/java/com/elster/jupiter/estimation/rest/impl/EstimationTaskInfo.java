/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EstimationTaskInfo {

    public long id = 0;
    public String name = "blank_name";
    public boolean revalidate;
    public boolean active = true;
    public MeterGroupInfo deviceGroup;
    public IdWithDisplayValueInfo<Long> usagePointGroup;
    public IdWithDisplayValueInfo<Long> metrologyPurpose;
    public PeriodicalExpressionInfo schedule;
    public RelativePeriodInfo period;
    public EstimationTaskHistoryInfo lastEstimationOccurrence;
    public Long nextRun;
    public Long lastRun;
    public long version;
    public String application;
    public String recurrence;
    public int logLevel = Level.WARNING.intValue();
    public List<TaskInfo> nextRecurrentTasks;
    public List<TaskInfo> previousRecurrentTasks;

    public EstimationTaskInfo() {
    }

    public EstimationTaskInfo(EstimationTask estimationTask, Thesaurus thesaurus, TimeService timeService) {
        this();
        populate(estimationTask);
        ScheduleExpression scheduleExpression = estimationTask.getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            schedule = null;
            recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                recurrence = fromTemporalExpression((TemporalExpression) scheduleExpression, timeService);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                recurrence = fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression, timeService);
            }
        }
        lastEstimationOccurrence = estimationTask.getLastOccurrence().map(occurrence -> new EstimationTaskHistoryInfo(estimationTask, occurrence, thesaurus)).orElse(null);
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression, TimeService timeService) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression, TimeService timeService) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    void populate(EstimationTask estimationTask) {
        id = estimationTask.getId();
        name = estimationTask.getName();
        revalidate = estimationTask.shouldRevalidate();
        logLevel = estimationTask.getLogLevel();
        active = estimationTask.isActive();
        deviceGroup =  estimationTask.getEndDeviceGroup().map(MeterGroupInfo::new).orElse(null);
        usagePointGroup = estimationTask.getUsagePointGroup().map(upg -> new IdWithDisplayValueInfo<>(upg.getId(), upg.getName())).orElse(null);
        metrologyPurpose = estimationTask.getMetrologyPurpose().map(mp -> new IdWithDisplayValueInfo<>(mp.getId(), mp.getName())).orElse(null);
        estimationTask.getPeriod().ifPresent(period -> this.period = RelativePeriodInfo.withCategories(period));

        Instant nextExecution = estimationTask.getNextExecution();
        if (nextExecution != null) {
            nextRun = nextExecution.toEpochMilli();
        }
        Optional<Instant> lastRunOptional = estimationTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            lastRun = lastRunOptional.get().toEpochMilli();
        }
        version = estimationTask.getVersion();
        nextRecurrentTasks = constructTaskInfo(estimationTask.getNextRecurrentTasks());
        previousRecurrentTasks = constructTaskInfo(estimationTask.getPrevRecurrentTasks());
    }

    private List<TaskInfo> constructTaskInfo(List<RecurrentTask> recurrentTasks) {
        return recurrentTasks.stream().map(recurrentTask -> TaskInfo.from(recurrentTask)).collect(Collectors.toList());
    }
}
