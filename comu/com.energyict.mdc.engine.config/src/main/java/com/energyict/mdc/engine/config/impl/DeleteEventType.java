/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (16:24)
 */
public enum DeleteEventType {

    COM_PORT_POOL(EventType.COMPORTPOOL_CREATED);

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}