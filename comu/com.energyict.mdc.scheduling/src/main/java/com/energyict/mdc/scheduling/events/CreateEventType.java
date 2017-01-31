/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.events;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:53)
 */
public enum CreateEventType {

    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}