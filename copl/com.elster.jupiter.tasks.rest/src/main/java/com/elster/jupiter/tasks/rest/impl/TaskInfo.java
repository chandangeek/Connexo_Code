/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by igh on 27/10/2015.
 */
public class TaskInfo {

    public String name;
    public IdWithNameInfo application;
    public String queue;
    public String queueStatus;
    public Long queueStatusDate;
    public String trigger;
    public Long nextRun;
    public Long currentRunDuration;
    public String lastRunStatus;
    public Long lastRunDate;
    public Long lastRunDuration;

    private static final String PLANNED = "Planned";
    private static final String BUSY = "Busy";

    TaskInfo(RecurrentTask recurrentTask, Thesaurus thesaurus, TimeService timeService, Locale locale, Clock clock) {
        name = recurrentTask.getName();
        application = new IdWithNameInfo(recurrentTask.getApplication(), thesaurus.getString(recurrentTask.getApplication(), recurrentTask.getApplication()));
        queue = recurrentTask.getDestination().getName();
        trigger = thesaurus.getFormat(TranslationKeys.SCHEDULED).format() + " (" + getScheduledTriggerDescription(recurrentTask.getScheduleExpression(), thesaurus, timeService, locale) + ")";
        Optional<TaskOccurrence> lastOccurrence = recurrentTask.getLastOccurrence();
        if (lastOccurrence.isPresent()) {
            TaskOccurrence occurrence = lastOccurrence.get();
            if (occurrence.getStatus().equals(TaskStatus.BUSY)) {
                setBusySince(recurrentTask, occurrence.getStartDate().get().toEpochMilli(), clock);
            } else if (occurrence.getStatus().equals(TaskStatus.NOT_EXECUTED_YET)) {
                setPlannedOn(recurrentTask, null);
            } else {
                setPlannedOn(recurrentTask, occurrence);
            }
        } else {
            setPlannedOn(recurrentTask, null);
        }
        nextRun = recurrentTask.getNextExecution().toEpochMilli();
    }

    private void setPlannedOn(RecurrentTask recurrentTask, TaskOccurrence lastOccurrence) {
        long plannedDate = recurrentTask.getNextExecution().toEpochMilli();
        setQueueStatus(PLANNED, plannedDate);
        if (lastOccurrence != null) {
            if (lastOccurrence.getStartDate().isPresent() && lastOccurrence.getEndDate().isPresent()) {
                setLastRunStatus(lastOccurrence);
            } else {
                // startdate is not set yet because task is not picked up by the queue yet, so we take the previous ocurence
                List<TaskOccurrence> occurences = recurrentTask.getTaskOccurrences();
                if (occurences.size() > 1) {
                    setLastOccurence(occurences);
                }
            }
        }
    }

    private void setLastRunStatus(TaskOccurrence lastOccurrence) {
        lastRunStatus = lastOccurrence.getStatus().toString();
        if (lastOccurrence.getStartDate().isPresent()) {
            lastRunDate = lastOccurrence.getStartDate().get().toEpochMilli();
            lastRunDuration = lastOccurrence.getEndDate().get().toEpochMilli() - lastOccurrence.getStartDate().get().toEpochMilli();
        }
    }

    private void setBusySince(RecurrentTask recurrentTask, Long startDate, Clock clock) {
        setQueueStatus(BUSY, startDate);
        // Take the previous occurence to check the last run status
        List<TaskOccurrence> occurences = recurrentTask.getTaskOccurrences();
        if (occurences.size() > 1) {
            setLastOccurence(occurences);
        }

        currentRunDuration = Instant.now(clock).toEpochMilli() - startDate;
    }

    private void setLastOccurence(List<TaskOccurrence> occurences) {
        Optional<TaskOccurrence> lastOcc = occurences.stream()
                .skip(1)
                .filter(occurence -> occurence.getStartDate().isPresent() && occurence.getEndDate().isPresent())
                .findFirst();
        if(lastOcc.isPresent()) {
            setLastRunStatus(lastOcc.get());
        }
    }

    private void setQueueStatus(String status, Long date) {
        queueStatus = status;
        queueStatusDate = date;
    }

    private String getScheduledTriggerDescription(ScheduleExpression scheduleExpression, Thesaurus thesaurus, TimeService timeService, Locale locale) {
        if (Never.NEVER.equals(scheduleExpression)) {
            return null;
        }
        if (scheduleExpression instanceof PeriodicalScheduleExpression) {
            return timeService.toLocalizedString((PeriodicalScheduleExpression) scheduleExpression);
        }
        if (scheduleExpression instanceof TemporalExpression) {
            return timeService.toLocalizedString((TemporalExpression) scheduleExpression);
        }
        if (scheduleExpression instanceof CronExpression) {
            return timeService.toLocalizedString((CronExpression) scheduleExpression, locale);
        }
        return scheduleExpression.toString();
    }

}