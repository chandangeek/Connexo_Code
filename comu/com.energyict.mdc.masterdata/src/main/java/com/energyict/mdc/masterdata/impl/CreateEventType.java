package com.energyict.mdc.masterdata.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:54)
 */
public enum CreateEventType {

    LOADPROFILETYPE(EventType.LOADPROFILETYPE_CREATED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_CREATED),
    REGISTERGROUP(EventType.REGISTERGROUP_CREATED),
    PHENOMENON(EventType.PHENOMENON_CREATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}