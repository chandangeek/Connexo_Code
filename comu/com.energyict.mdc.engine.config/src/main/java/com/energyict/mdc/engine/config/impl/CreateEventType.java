/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (16:25)
 */
public enum CreateEventType {

    COM_PORT_POOL(EventType.COMPORTPOOL_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}