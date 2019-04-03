package com.elster.jupiter.issue.task.rest.response;

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

    public TaskOccurrenceInfo(Thesaurus thesaurus, TaskOccurrence occurrence) {
        this.id = occurrence.getId();
        this.recurrentTask = RecurrentTaskInfo.from(thesaurus, occurrence.getRecurrentTask());
        this.triggerTime = occurrence.getTriggerTime();
        this.startDate = occurrence.getStartDate().orElse(null);
        this.enddate = occurrence.getEndDate().orElse(null);
        this.status = occurrence.getStatus().getDefaultFormat();
    }
}
