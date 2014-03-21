package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum UpdateEventType {

    CONNECTIONTASK(EventType.CONNECTIONTASK_UPDATED),
    CONNECTIONMETHOD(EventType.CONNECTIONMETHOD_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}