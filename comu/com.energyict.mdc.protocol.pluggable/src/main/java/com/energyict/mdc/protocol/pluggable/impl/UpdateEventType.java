/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

/**
 * Subset of {@link EventType}s that relate to updating persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (16:05)
 */
public enum UpdateEventType {
    CONNECTIONTYPE(EventType.CONNECTIONTYPE_UPDATED),
    DEVICEPROTOCOL(EventType.DEVICEPROTOCOL_UPDATED),
    INBOUNDEVICEPROTOCOL(EventType.INBOUNDDEVICEPROTOCOL_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}