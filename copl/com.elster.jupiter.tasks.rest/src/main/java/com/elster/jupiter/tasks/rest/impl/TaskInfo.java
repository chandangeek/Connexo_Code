package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Created by igh on 27/10/2015.
 */
public class TaskInfo {

    public String name;
    public String application;
    public String queue;
    public String queueStatus;
    public Long queueStatusDate;
    public Long nextRun;
    public Long currentRunDuration;
    public String lastRunStatus;
    public Long lastRunDate;
    public Long lastRunDuration;


    static String PLANNED = "Planned";
    static String BUSY = "Busy";

    public TaskInfo(RecurrentTask recurrentTask) {
        name = recurrentTask.getName();
        application = "Multisense";
        queue = recurrentTask.getDestination().getName();
        Optional<TaskOccurrence> lastOccurrence = recurrentTask.getLastOccurrence();
        if (lastOccurrence.isPresent()) {
            TaskOccurrence occurrence = lastOccurrence.get();
            if (occurrence.getStatus().equals(TaskStatus.BUSY)) {
                setBusySince(occurrence.getStartDate().get().toEpochMilli());
            } else {
                setPlannedOn(recurrentTask.getNextExecution().toEpochMilli(), occurrence);
            }
        } else {
            setPlannedOn(recurrentTask.getNextExecution().toEpochMilli(), null);
        }
        nextRun = recurrentTask.getNextExecution().toEpochMilli();
    }

    private void setPlannedOn(Long plannedDate, TaskOccurrence lastOccurrence) {
        setQueueStatus(PLANNED, plannedDate);
        if (lastOccurrence != null) {
            lastRunStatus = lastOccurrence.getStatus().toString();
            lastRunDate = lastOccurrence.getStartDate().get().toEpochMilli();
            lastRunDuration = lastOccurrence.getEndDate().get().toEpochMilli() - lastOccurrence.getStartDate().get().toEpochMilli();
        }
    }

    private void setBusySince(Long startDate) {
        setQueueStatus(BUSY, startDate);
        currentRunDuration = Instant.now().toEpochMilli() - startDate;
    }

    private void setQueueStatus(String status, Long date) {
        queueStatus = status;
        queueStatusDate = date;
    }
}
