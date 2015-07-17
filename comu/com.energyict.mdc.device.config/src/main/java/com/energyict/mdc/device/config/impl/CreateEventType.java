package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.events.EventType;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:53)
 */
public enum CreateEventType {

    DEVICETYPE(EventType.DEVICETYPE_CREATED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_CREATED),
    COMTASKENABLEMENT(EventType.COMTASKENABLEMENT_CREATED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_CREATED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_CREATED),
    CHANNELSPEC(EventType.CHANNELSPEC_CREATED),
    REGISTERSPEC(EventType.REGISTERSPEC_CREATED),
    PROTOCOLCONFIGPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_CREATED),
    PARTIAL_INBOUND_CONNECTION_TASK(EventType.PARTIAL_INBOUND_CONNECTION_TASK_CREATED),
    PARTIAL_SCHEDULED_CONNECTION_TASK(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_CREATED),
    PARTIAL_CONNECTION_INITIATION_TASK(EventType.PARTIAL_CONNECTION_INITIATION_TASK_CREATED),
    DEVICE_COMMUNICATION_CONFIGURATION(EventType.DEVICE_COMMUNICATION_CONFIGURATION_CREATED),
    SECURITY_PROPERTY_SET(EventType.SECURITY_PROPERTY_SET_CREATED),
    DEVICE_MESSAGE_ENABLEMENT(EventType.DEVICE_MESSAGE_ENABLEMENT_CREATED);


    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}