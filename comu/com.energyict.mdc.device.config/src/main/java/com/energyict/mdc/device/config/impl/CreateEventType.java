package com.energyict.mdc.device.config.impl;

/**
 * Subset of {@link EventType}s that relate to creation of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:53)
 */
public enum CreateEventType {

    DEVICETYPE(EventType.DEVICETYPE_CREATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_CREATED),
    LOADPROFILETYPE(EventType.LOADPROFILETYPE_CREATED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_CREATED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_CREATED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_CREATED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_CREATED),
    CHANNELSPEC(EventType.CHANNELSPEC_CREATED),
    REGISTERSPEC(EventType.REGISTERSPEC_CREATED),
    REGISTERGROUP(EventType.REGISTERGROUP_CREATED),
    PHENOMENON(EventType.PHENOMENON_CREATED),
    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_CREATED),
    PROTOCOLCONFIGPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_CREATED),
    PARTIAL_INBOUND_CONNECTION_TASK(EventType.PARTIAL_INBOUND_CONNECTION_TASK_CREATED),
    PARTIAL_OUTBOUND_CONNECTION_TASK(EventType.PARTIAL_OUTBOUND_CONNECTION_TASK_CREATED),
    PARTIAL_CONNECTION_INITIATION_TASK(EventType.PARTIAL_CONNECTION_INITIATION_TASK_CREATED),
    DEVICE_COMMUNICATION_CONFIGURATION(EventType.DEVICE_COMMUNICATION_CONFIGURATION_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}