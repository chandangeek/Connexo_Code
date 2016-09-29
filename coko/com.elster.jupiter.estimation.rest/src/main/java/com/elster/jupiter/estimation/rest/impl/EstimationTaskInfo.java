package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.Optional;

public class EstimationTaskInfo {

    public long id = 0;
    public String name = "blank_name";
    public boolean active = true;
    public MeterGroupInfo deviceGroup;
    public PeriodicalExpressionInfo schedule;
    public RelativePeriodInfo period;
    public EstimationTaskHistoryInfo lastEstimationOccurrence;
    public Long nextRun;
    public Long lastRun;
    public long version;
    // TODO: remove, not needed
    public String application;

    public EstimationTaskInfo() {
    }

    public EstimationTaskInfo(EstimationTask estimationTask, Thesaurus thesaurus) {
        populate(estimationTask);
        if (Never.NEVER.equals(estimationTask.getScheduleExpression())) {
            schedule = null;
        } else {
            ScheduleExpression scheduleExpression = estimationTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        lastEstimationOccurrence = estimationTask.getLastOccurrence().map(occurrence -> new EstimationTaskHistoryInfo(estimationTask, occurrence, thesaurus)).orElse(null);
    }

    void populate(EstimationTask estimationTask) {
        id = estimationTask.getId();
        name = estimationTask.getName();
        active = estimationTask.isActive();
        deviceGroup = new MeterGroupInfo(estimationTask.getEndDeviceGroup());
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
    }

}
