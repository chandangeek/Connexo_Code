/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.events;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:44)
 */
public enum DeleteEventType {

    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_DELETED),
    COMSCHEDULES(EventType.COMSCHEDULES_DELETED),
    ;

    private EventType eventType;

    DeleteEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}