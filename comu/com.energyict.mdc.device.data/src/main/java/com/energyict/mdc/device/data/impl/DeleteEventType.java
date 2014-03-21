package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum DeleteEventType {

    CONNECTIONTASK(EventType.CONNECTIONTASK_DELETED),
    CONNECTIONMETHOD(EventType.CONNECTIONMETHOD_DELETED);

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}