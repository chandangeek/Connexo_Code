/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.EventType;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:57)
 */
public enum UpdateEventType {

    DEVICETYPE(EventType.DEVICETYPE_UPDATED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_UPDATED),
    COMTASKENABLEMENT(EventType.COMTASKENABLEMENT_UPDATED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_UPDATED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_UPDATED),
    CHANNELSPEC(EventType.CHANNELSPEC_UPDATED),
    REGISTERSPEC(EventType.REGISTERSPEC_UPDATED),
    PROTOCOLCONFIGPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_UPDATED),
    PARTIAL_INBOUND_CONNECTION_TASK(EventType.PARTIAL_INBOUND_CONNECTION_TASK_UPDATED),
    PARTIAL_SCHEDULED_CONNECTION_TASK(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_UPDATED),
    PARTIAL_CONNECTION_INITIATION_TASK(EventType.PARTIAL_CONNECTION_INITIATION_TASK_UPDATED),
    DEVICE_COMMUNICATION_CONFIGURATION(EventType.DEVICE_COMMUNICATION_CONFIGURATION_UPDATED),
    SECURITY_PROPERTY_SET(EventType.SECURITY_PROPERTY_SET_UPDATED),
    DEVICE_MESSAGE_ENABLEMENT(EventType.DEVICE_MESSAGE_ENABLEMENT_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}