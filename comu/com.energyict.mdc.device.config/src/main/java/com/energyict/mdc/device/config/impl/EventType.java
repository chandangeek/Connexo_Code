package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.config.DeviceConfigurationService;

/**
 * Models the different event types that are produced by this "device type and configurations bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:44)
 */
public enum EventType {

    DEVICETYPE_CREATED("devicetype/CREATED"),
    DEVICETYPE_UPDATED("devicetype/UPDATED"),
    DEVICETYPE_DELETED("devicetype/DELETED"),
    LOGBOOKTYPE_CREATED("logbooktype/CREATED"),
    LOGBOOKTYPE_UPDATED("logbooktype/UPDATED"),
    LOGBOOKTYPE_DELETED("logbooktype/DELETED"),
    LOADPROFILETYPE_CREATED("loadprofiletype/CREATED"),
    LOADPROFILETYPE_UPDATED("loadprofiletype/UPDATED"),
    LOADPROFILETYPE_DELETED("loadprofiletype/DELETED"),
    REGISTERMAPPING_CREATED("registermapping/CREATED"),
    REGISTERMAPPING_UPDATED("registermapping/UPDATED"),
    REGISTERMAPPING_DELETED("registermapping/DELETED"),
    DEVICECONFIGURATION_CREATED("deviceconfiguration/CREATED"),
    DEVICECONFIGURATION_UPDATED("deviceconfiguration/UPDATED"),
    DEVICECONFIGURATION_DELETED("deviceconfiguration/DELETED"),
    LOGBOOKSPEC_CREATED("logbookspec/CREATED"),
    LOGBOOKSPEC_UPDATED("logbookspec/UPDATED"),
    LOGBOOKSPEC_DELETED("logbookspec/DELETED"),
    LOADPROFILESPEC_CREATED("loadprofilespec/CREATED"),
    LOADPROFILESPEC_UPDATED("loadprofilespec/UPDATED"),
    LOADPROFILESPEC_DELETED("loadprofilespec/DELETED"),
    CHANNELSPEC_CREATED("channelspec/CREATED"),
    CHANNELSPEC_UPDATED("channelspec/UPDATED"),
    CHANNELSPEC_DELETED("channelspec/DELETED"),
    REGISTERSPEC_CREATED("registerspec/CREATED"),
    REGISTERSPEC_UPDATED("registerspec/UPDATED"),
    REGISTERSPEC_DELETED("registerspec/DELETED"),
    REGISTERGROUP_CREATED("registergroup/CREATED"),
    REGISTERGROUP_UPDATED("registergroup/UPDATED"),
    REGISTERGROUP_DELETED("registergroup/DELETED"),
    PHENOMENON_CREATED("phenomenon/CREATED"),
    PHENOMENON_UPDATED("phenomenon/UPDATED"),
    PHENOMENON_DELETED("phenomenon/DELETED"),
    NEXTEXECUTIONSPECS_CREATED("nextexecutionspecs/CREATED"),
    NEXTEXECUTIONSPECS_UPDATED("nextexecutionspecs/UPDATED"),
    NEXTEXECUTIONSPECS_DELETED("nextexecutionspecs/DELETED");

    private static final String NAMESPACE = "com/energyict/mdc/device/config/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(DeviceConfigurationService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .shouldPublish()
                .withProperty("id", ValueType.LONG, "id");
        this.addCustomProperties(builder).create().save();
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}