package com.energyict.mdc.masterdata.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:56)
 */
public enum DeleteEventType {

    LOADPROFILETYPE(EventType.LOADPROFILETYPE_DELETED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_DELETED),
    REGISTERGROUP(EventType.REGISTERGROUP_DELETED),
    PHENOMENON(EventType.PHENOMENON_DELETED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_DELETED);

    private EventType eventType;

    DeleteEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}