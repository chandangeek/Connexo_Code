package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to updates of persistent objects.
 * <p/>
 * Copyrights EnergyICT
 * Date: 03/03/14
 * Time: 16:05
 */
public enum UpdateEventType {

    DEVICE(EventType.DEVICE_UPDATED),
    LOADPROFILE(EventType.LOADPROFILE_UPDATED),
    LOGBOOK(EventType.LOGBOOK_UPDATED),;

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }
}
