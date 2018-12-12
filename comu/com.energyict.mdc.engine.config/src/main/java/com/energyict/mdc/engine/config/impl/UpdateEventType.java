/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:57)
 */
public enum UpdateEventType {

    COM_PORT_POOL(EventType.COMPORTPOOL_CREATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}