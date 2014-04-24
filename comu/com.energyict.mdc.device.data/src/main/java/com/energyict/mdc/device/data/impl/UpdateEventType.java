package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum UpdateEventType {

    DEVICE(EventType.DEVICE_UPDATED),
    LOADPROFILE(EventType.LOADPROFILE_UPDATED),
    LOGBOOK(EventType.LOGBOOK_UPDATED),
    CONNECTIONTASK(EventType.CONNECTIONTASK_UPDATED),
    CONNECTIONMETHOD(EventType.CONNECTIONMETHOD_UPDATED),
    PROTOCOLDIALECTPROPERTIES(EventType.PROTOCOLDIALECTPROPERTIES_UPDATED),
    COMTASKEXECUTION(EventType.COMTASKEXECUTION_UPDATED),
    ;

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}