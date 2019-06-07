/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.events.EventService;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface TaskExecutor {

    void execute(TaskOccurrence occurrence);

    default void postExecute(TaskOccurrence occurrence) {
    }

    default void postFailEvent(EventService eventService, TaskOccurrence occurrence, String cause){
        eventService.postEvent(EventType.TASK_OCCURRENCE_FAILED.topic(),
                TaskOccurrenceEventInfo.forFailure(occurrence, cause));
    }
}
