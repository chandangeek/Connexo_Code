/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.Optional;

public class EstimationTaskHistoryInfo {

    public Long id;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public Long lastRun;
    public Long statusDate;
    public String statusPrefix;
    public EstimationTaskInfo task;

    public EstimationTaskHistoryInfo() {
    }

    public EstimationTaskHistoryInfo(EstimationTask estimationTask, TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        populate(estimationTask, taskOccurrence, thesaurus);
    }

    private void populate(EstimationTask estimationTask, TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        this.id = taskOccurrence.getId();
        this.startedOn = taskOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = taskOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = taskOccurrence.getStatusName();
        this.lastRun = taskOccurrence.getTriggerTime().toEpochMilli();
        setStatusOnDate(taskOccurrence, thesaurus);
        History<EstimationTask> estTaskHistory = estimationTask.getHistory();

        EstimationTask version = estTaskHistory.getVersionAt(taskOccurrence.getTriggerTime())
                .orElseGet(() -> estTaskHistory.getVersionAt(taskOccurrence.getRecurrentTask().getCreateTime())
                        .orElse(estimationTask));

        task = new EstimationTaskInfo();
        task.populate(version);

        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(taskOccurrence.getTriggerTime());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
    }

    private Long toLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

    private void setStatusOnDate(TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        TaskStatus taskStatus = taskOccurrence.getStatus();
        String statusTranslation = taskOccurrence.getStatusName();
        if (TaskStatus.BUSY.equals(taskStatus)) {
            this.statusPrefix = thesaurus.getFormat(TranslationKeys.SINCE).format(statusTranslation);
            this.statusDate = startedOn;
        } else if ((TaskStatus.FAILED.equals(taskStatus)) || (TaskStatus.SUCCESS.equals(taskStatus))) {
            this.statusPrefix = thesaurus.getFormat(TranslationKeys.ON).format(statusTranslation);
            this.statusDate = finishedOn;
        } else {
            this.statusPrefix = statusTranslation;
        }
    }

}