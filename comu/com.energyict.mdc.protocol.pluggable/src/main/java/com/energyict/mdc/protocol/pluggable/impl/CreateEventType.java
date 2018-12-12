/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:05)
 */
public enum CreateEventType {
    CONNECTIONTYPE(EventType.CONNECTIONTYPE_CREATED),
    DEVICEPROTOCOL(EventType.DEVICEPROTOCOL_CREATED),
    INBOUNDEVICEPROTOCOL(EventType.INBOUNDDEVICEPROTOCOL_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}