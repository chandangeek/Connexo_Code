package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.issue.task.RelatedTaskOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;

public class TaskOccurrenceInfo {


    public long id;
    public RecurrentTaskInfo recurrentTask;
    public Instant triggerTime;
    public Instant startDate;
    public Instant enddate;
    public String status;
    public String errorMessage;
    public Instant failureTime;

    public TaskOccurrenceInfo() {
    }

    public static TaskOccurrenceInfo from (Thesaurus thesaurus, RelatedTaskOccurrence relatedTaskOccurrence) {
        TaskOccurrenceInfo taskOccurrence = new TaskOccurrenceInfo();
        TaskOccurrence occurrence = relatedTaskOccurrence.getTaskOccurrence();
        taskOccurrence.id = occurrence.getId();
        taskOccurrence.triggerTime = occurrence.getTriggerTime();
        taskOccurrence.startDate = occurrence.getStartDate().orElse(null);
        taskOccurrence.enddate = occurrence.getEndDate().orElse(null);
        taskOccurrence.status = occurrence.getStatus().getDefaultFormat();
        taskOccurrence.errorMessage = relatedTaskOccurrence.getErrorMessage();
        taskOccurrence.failureTime = relatedTaskOccurrence.getFailureTime();
        return taskOccurrence;
    }
}
