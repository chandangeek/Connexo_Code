package com.energyict.mdc.masterdata.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:55)
 */
public enum UpdateEventType {

    LOADPROFILETYPE(EventType.LOADPROFILETYPE_UPDATED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_UPDATED),
    REGISTERGROUP(EventType.REGISTERGROUP_UPDATED),
    PHENOMENON(EventType.PHENOMENON_UPDATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}