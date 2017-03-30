/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum DeleteEventType {

    CONNECTIONTASK(EventType.CONNECTIONTASK_DELETED),
    PROTOCOLDIALECTPROPERTIES(EventType.PROTOCOLDIALECTPROPERTIES_DELETED),
    DEVICE(EventType.DEVICE_DELETED),
    LOADPROFILE(EventType.LOADPROFILE_DELETED),
    LOGBOOK(EventType.LOGBOOK_DELETED),
    COMTASKEXECUTION(EventType.COMTASKEXECUTION_DELETED),
    DEVICEMESSAGE(EventType.DEVICEMESSAGE_DELETED),
    COMTASKEXECUTIONTRIGGER(EventType.COMTASKEXECUTIONTRIGGER_DELETED),
    ;

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}