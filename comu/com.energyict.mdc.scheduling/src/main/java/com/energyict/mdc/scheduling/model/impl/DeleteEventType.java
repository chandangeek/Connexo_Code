package com.energyict.mdc.scheduling.model.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:44)
 */
public enum DeleteEventType {

    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_DELETED);

    private EventType eventType;

    DeleteEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}