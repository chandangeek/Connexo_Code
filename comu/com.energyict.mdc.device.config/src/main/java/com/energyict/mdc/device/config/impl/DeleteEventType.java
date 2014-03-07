package com.energyict.mdc.device.config.impl;

/**
 * Subset of {@link EventType}s that relate to deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:44)
 */
public enum DeleteEventType {

    DEVICETYPE(EventType.DEVICETYPE_DELETED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_DELETED),
    LOADPROFILETYPE(EventType.LOADPROFILETYPE_DELETED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_DELETED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_DELETED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_DELETED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_DELETED),
    CHANNELSPEC(EventType.CHANNELSPEC_DELETED),
    REGISTERSPEC(EventType.REGISTERSPEC_DELETED),
    REGISTERGROUP(EventType.REGISTERGROUP_DELETED),
    PHENOMENON(EventType.PHENOMENON_DELETED),
    PROTOCOLCONFIGPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_DELETED);

    private EventType eventType;

    DeleteEventType (EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}