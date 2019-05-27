package com.elster.jupiter.tasks;
import java.time.Clock;

public class TaskOccurrenceEventInfo {

        private long taskOccurrenceId;
        private String errorMessage;
        private long failureTime;

    public long getTaskOccurrenceId() {
        return taskOccurrenceId;
    }

    public void setTaskOccurrenceId(long taskOccurrenceId) {
        this.taskOccurrenceId = taskOccurrenceId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getFailureTime() {
        return failureTime;
    }

    public void setFailureTime(long failureTime) {
        this.failureTime = failureTime;
    }

    public static TaskOccurrenceEventInfo forFailure(TaskOccurrence taskOccurrence, String cause) {
        TaskOccurrenceEventInfo eventInfo = new TaskOccurrenceEventInfo();
            eventInfo.setTaskOccurrenceId(taskOccurrence.getId());
            eventInfo.setErrorMessage(cause);
            eventInfo.setFailureTime(Clock.systemDefaultZone().instant().toEpochMilli());
            return eventInfo;
        }
    }