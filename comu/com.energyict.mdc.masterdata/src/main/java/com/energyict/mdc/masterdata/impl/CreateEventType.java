/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:54)
 */
public enum CreateEventType {

    LOADPROFILETYPE(EventType.LOADPROFILETYPE_CREATED),
    MEASUREMENTTYPE(EventType.MEASUREMENTTYPE_CREATED),
    REGISTERGROUP(EventType.REGISTERGROUP_CREATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}