package com.energyict.mdc.device.config.impl;

/**
 * Subset of {@link EventType}s that relate to update of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-18 (15:57)
 */
public enum UpdateEventType {

    DEVICETYPE(EventType.DEVICETYPE_UPDATED),
    LOGBOOKTYPE(EventType.LOGBOOKTYPE_UPDATED),
    LOADPROFILETYPE(EventType.LOADPROFILETYPE_UPDATED),
    REGISTERMAPPING(EventType.REGISTERMAPPING_UPDATED),
    DEVICECONFIGURATION(EventType.DEVICECONFIGURATION_UPDATED),
    LOGBOOKSPEC(EventType.LOGBOOKSPEC_UPDATED),
    LOADPROFILESPEC(EventType.LOADPROFILESPEC_UPDATED),
    CHANNELSPEC(EventType.CHANNELSPEC_UPDATED),
    REGISTERSPEC(EventType.REGISTERSPEC_UPDATED),
    REGISTERGROUP(EventType.REGISTERGROUP_UPDATED),
    PHENOMENON(EventType.PHENOMENON_UPDATED),
    NEXTEXECUTIONSPECS(EventType.NEXTEXECUTIONSPECS_UPDATED);

    private EventType eventType;

    UpdateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}