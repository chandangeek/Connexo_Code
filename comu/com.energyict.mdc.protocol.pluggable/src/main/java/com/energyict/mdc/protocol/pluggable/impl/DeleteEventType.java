/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:05)
 */
public enum DeleteEventType {
    CONNECTIONTYPE(EventType.CONNECTIONTYPE_DELETED),
    DEVICEPROTOCOL(EventType.DEVICEPROTOCOL_DELETED),
    INBOUNDEVICEPROTOCOL(EventType.INBOUNDDEVICEPROTOCOL_DELETED);

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}