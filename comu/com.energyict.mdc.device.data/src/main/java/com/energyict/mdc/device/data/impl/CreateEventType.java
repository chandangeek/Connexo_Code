/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum CreateEventType {

    DEVICE(EventType.DEVICE_CREATED),
    LOADPROFILE(EventType.LOADPROFILE_CREATED),
    LOGBOOK(EventType.LOGBOOK_CREATED),
    CONNECTIONTASK(EventType.CONNECTIONTASK_CREATED),
    PROTOCOLDIALECTPROPERTIES(EventType.PROTOCOLDIALECTPROPERTIES_CREATED),
    COMTASKEXECUTION(EventType.COMTASKEXECUTION_CREATED),
    DEVICEMESSAGE(EventType.DEVICEMESSAGE_CREATED),
    COMTASKEXECUTIONTRIGGER(EventType.COMTASKEXECUTIONTRIGGER_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}