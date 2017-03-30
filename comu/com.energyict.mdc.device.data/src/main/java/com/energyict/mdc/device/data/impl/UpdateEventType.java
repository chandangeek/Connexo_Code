/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum UpdateEventType {

    DEVICE(EventType.DEVICE_UPDATED),
    LOADPROFILE(EventType.LOADPROFILE_UPDATED),
    LOGBOOK(EventType.LOGBOOK_UPDATED),
    CONNECTIONTASK(EventType.CONNECTIONTASK_UPDATED),
    PROTOCOLDIALECTPROPERTIES(EventType.PROTOCOLDIALECTPROPERTIES_UPDATED),
    COMTASKEXECUTION(EventType.COMTASKEXECUTION_UPDATED),
    COMSCHEDULE(EventType.COMSCHEDULE_UPDATED),
    DEVICEMESSAGE(EventType.DEVICEMESSAGE_UPDATED),
    COMTASKEXECUTIONTRIGGER(EventType.COMTASKEXECUTIONTRIGGER_UPDATED),
    ;

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}