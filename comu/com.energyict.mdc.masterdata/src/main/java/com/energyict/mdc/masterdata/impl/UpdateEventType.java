/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:55)
 */
public enum UpdateEventType {

    LOADPROFILETYPE(EventType.LOADPROFILETYPE_UPDATED),
    MEASUREMENTTYPE(EventType.MEASUREMENTTYPE_UPDATED),
    REGISTERGROUP(EventType.REGISTERGROUP_UPDATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}