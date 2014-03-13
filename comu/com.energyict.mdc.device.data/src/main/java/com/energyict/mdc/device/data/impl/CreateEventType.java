package com.energyict.mdc.device.data.impl;


/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:53)
 */
public enum CreateEventType {

    DEVICE(EventType.DEVICE_CREATED),
    LOADPROFILE(EventType.LOADPROFILE_CREATED),
    LOGBOOK(EventType.LOGBOOK_CREATED),
    ;

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}
