package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to deleting persistent objects.
 *
 * Copyrights EnergyICT
 * Date: 03/03/14
 * Time: 16:06
 */
public enum DeleteEventType {

    DEVICE(EventType.DEVICE_DELETED),
    LOADPROFILE(EventType.LOADPROFILE_DELETED),
    LOGBOOK(EventType.LOGBOOK_DELETED),;

    private EventType eventType;

    DeleteEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }
}
