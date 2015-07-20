package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.events.EventType;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:44)
 */
public enum DeleteEventType {

    DEVICETYPE(EventType.DEVICETYPE_DELETED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_DELETED),
    COMTASKENABLEMENT(EventType.COMTASKENABLEMENT_DELETED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_DELETED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_DELETED),
    CHANNELSPEC(EventType.CHANNELSPEC_DELETED),
    REGISTERSPEC(EventType.REGISTERSPEC_DELETED),
    PROTOCOLCONFIGPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_DELETED),
    PARTIAL_INBOUND_CONNECTION_TASK(EventType.PARTIAL_INBOUND_CONNECTION_TASK_DELETED),
    PARTIAL_SCHEDULED_CONNECTION_TASK(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_DELETED),
    PARTIAL_CONNECTION_INITIATION_TASK(EventType.PARTIAL_CONNECTION_INITIATION_TASK_DELETED),
    DEVICE_COMMUNICATION_CONFIGURATION(EventType.DEVICE_COMMUNICATION_CONFIGURATION_DELETED),
    SECURITY_PROPERTY_SET(EventType.SECURITY_PROPERTY_SET_DELETED),
    DEVICE_MESSAGE_ENABLEMENT(EventType.DEVICE_MESSAGE_ENABLEMENT_DELETE);

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}