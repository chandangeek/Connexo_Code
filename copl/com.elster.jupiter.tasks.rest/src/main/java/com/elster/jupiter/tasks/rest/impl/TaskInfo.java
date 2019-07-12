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

    private static final String PLANNED = "Planned";
    private static final String BUSY = "Busy";
    private static final String NOTSCHEDULED = "Not scheduled";
    public Long id;
    public String name;
    public IdWithNameInfo application;
    public String queue;
    public String queueType;
    public String queueStatus;
    public Long queueStatusDate;
    public String trigger;
    public Long nextRun;
    public Long suspendUntilTime;
    public Long currentRunDuration;
    public String lastRunStatus;
    public Long lastRunDate;
    public Long lastRunDuration;
    public Boolean extraQueueCreationEnabled;
    public Boolean queuePrioritized;
    public Integer priority;

    public TaskInfo(){}  // needed for serialization - deserialization
    TaskInfo(RecurrentTask recurrentTask, Thesaurus thesaurus, TimeService timeService, Locale locale, Clock clock) {
        id = recurrentTask.getId();
        name = recurrentTask.getName();
        application = new IdWithNameInfo(recurrentTask.getApplication(), thesaurus.getString(recurrentTask.getApplication(), recurrentTask.getApplication()));
        queue = recurrentTask.getDestination().getName();
        queueType = recurrentTask.getDestination().getQueueTypeName();
        extraQueueCreationEnabled = recurrentTask.getDestination().isExtraQueueCreationEnabled();
        queuePrioritized = recurrentTask.getDestination().isPrioritized();
        priority = recurrentTask.getPriority();
        trigger = Never.NEVER.equals(recurrentTask.getScheduleExpression()) ? thesaurus.getFormat(TranslationKeys.NOTSCHEDULED).format() :
                thesaurus.getFormat(TranslationKeys.SCHEDULED).format() + " (" + getScheduledTriggerDescription(recurrentTask.getScheduleExpression(), thesaurus, timeService, locale) + ")";
        Optional<TaskOccurrence> lastOccurrence = recurrentTask.getLastOccurrence();
        if (lastOccurrence.isPresent()) {
            TaskOccurrence occurrence = lastOccurrence.get();
            if (occurrence.getStatus().equals(TaskStatus.BUSY)) {
                setBusySince(recurrentTask, occurrence.getStartDate().get().toEpochMilli(), clock);
                // } else if (occurrence.getStatus().equals(TaskStatus.NOT_EXECUTED_YET)) {
                //     setPlannedOn(recurrentTask, null);
            } else if (recurrentTask.getNextExecution() == null) {
                setNotScheduled(recurrentTask, occurrence);
            } else {
                setPlannedOn(recurrentTask, occurrence);
            }
        } else if (recurrentTask.getNextExecution() == null) {
            setNotScheduled(recurrentTask, null);
        } else {
            setPlannedOn(recurrentTask, null);
        }
        nextRun = recurrentTask.getNextExecution() != null ? recurrentTask.getNextExecution().toEpochMilli() : null;
        suspendUntilTime = recurrentTask.getSuspendUntil() != null ? recurrentTask.getSuspendUntil().toEpochMilli() : null;
    }

    private void setNotScheduled(RecurrentTask recurrentTask, TaskOccurrence lastOccurrence) {
        setQueueStatus(NOTSCHEDULED, null);
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

    private void setPlannedOn(RecurrentTask recurrentTask, TaskOccurrence lastOccurrence) {
        long plannedDate = getNextExecutionPlannedDate(recurrentTask);
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

    private long getNextExecutionPlannedDate(RecurrentTask recurrentTask) {
        if (recurrentTask.getNextExecution() == null) {
            return 0;
        }
        return recurrentTask.getNextExecution().toEpochMilli();
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
        if (lastOcc.isPresent()) {
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