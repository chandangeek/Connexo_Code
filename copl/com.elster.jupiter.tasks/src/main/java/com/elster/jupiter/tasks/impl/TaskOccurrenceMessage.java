package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.TaskOccurrence;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaskOccurrenceMessage {

    public long taskOccurrenceId;

    private TaskOccurrenceMessage() {
    }

    public TaskOccurrenceMessage(TaskOccurrence taskOccurrence) {
        taskOccurrenceId = taskOccurrence.getId();
    }


}
