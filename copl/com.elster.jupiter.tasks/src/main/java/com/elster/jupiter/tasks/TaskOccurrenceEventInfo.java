package com.elster.jupiter.tasks;
import java.time.Clock;

public class TaskOccurrenceEventInfo {

        private long taskOccurrenceId;
        private String errorMsg;
        private long timeStamp;

    public long getTaskOccurrenceId() {
        return taskOccurrenceId;
    }

    public void setTaskOccurrenceId(long taskOccurrenceId) {
        this.taskOccurrenceId = taskOccurrenceId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public static TaskOccurrenceEventInfo forFailure(TaskOccurrence taskOccurrence, String cause) {
        TaskOccurrenceEventInfo eventInfo = new TaskOccurrenceEventInfo();
            eventInfo.setTaskOccurrenceId(taskOccurrence.getId());
            eventInfo.setErrorMsg(cause);
            eventInfo.setTimeStamp(Clock.systemDefaultZone().instant().toEpochMilli());
            return eventInfo;
        }
    }