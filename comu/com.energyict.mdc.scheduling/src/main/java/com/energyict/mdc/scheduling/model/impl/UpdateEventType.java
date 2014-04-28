package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.events.EventType;

/**
 * Subset of {@link com.energyict.mdc.scheduling.events.EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:57)
 */
public enum UpdateEventType {

    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}