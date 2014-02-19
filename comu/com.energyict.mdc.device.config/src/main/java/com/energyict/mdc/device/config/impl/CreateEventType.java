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
    PHENOMENON(EventType.PHENOMENON_CREATED);

    private EventType eventType;

    CreateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}