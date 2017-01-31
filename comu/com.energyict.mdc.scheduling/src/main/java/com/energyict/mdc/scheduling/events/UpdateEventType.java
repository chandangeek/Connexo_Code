/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.events;

/**
 * Subset of {@link com.energyict.mdc.scheduling.events.EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:57)
 */
public enum UpdateEventType {

    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_UPDATED),
    COMSCHEDULES(EventType.COMSCHEDULES_UPDATED),
    COMTASK_WILL_BE_ADDED_TO_SCHEDULE(EventType.COMTASK_WILL_BE_ADDED_TO_SCHEDULE);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}