/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.TaskOccurrence;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TaskOccurrenceMessage {

    public long taskOccurrenceId;

    @SuppressWarnings("unused")
	private TaskOccurrenceMessage() {
    }

    public TaskOccurrenceMessage(TaskOccurrence taskOccurrence) {
        taskOccurrenceId = taskOccurrence.getId();
    }


}
