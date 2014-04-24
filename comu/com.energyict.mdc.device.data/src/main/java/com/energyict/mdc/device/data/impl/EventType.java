package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.data.DeviceDataService;

/**
 * Models the different event types that are produced by this "device data bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum EventType {

    CONNECTIONTASK_CREATED("connectiontask/CREATED"),
    CONNECTIONTASK_UPDATED("connectiontask/UPDATED"),
    CONNECTIONTASK_DELETED("connectiontask/DELETED"),
    CONNECTIONMETHOD_CREATED("connectionmethod/CREATED"),
    CONNECTIONMETHOD_UPDATED("connectionmethod/UPDATED"),
    CONNECTIONMETHOD_DELETED("connectionmethod/DELETED"),
    DEVICE_CREATED("device/CREATED"),
    DEVICE_DELETED("device/DELETED"),
    DEVICE_UPDATED("device/UPDATED"),
    LOADPROFILE_CREATED("loadprofile/CREATED"),
    LOADPROFILE_DELETED("loadprofile/DELETED"),
    LOADPROFILE_UPDATED("loadprofile/UPDATED"),
    LOGBOOK_CREATED("logbook/CREATED"),
    LOGBOOK_DELETED("logbook/DELETED"),
    LOGBOOK_UPDATED("logbook/UPDATED"),
    PROTOCOLDIALECTPROPERTIES_CREATED("protocoldialectproperties/CREATED"),
    PROTOCOLDIALECTPROPERTIES_UPDATED("protocoldialectproperties/UPDATED"),
    PROTOCOLDIALECTPROPERTIES_DELETED("protocoldialectproperties/DELETED"),
    COMTASKEXECUTION_CREATED("comtaskexecution/CREATED"),
    COMTASKEXECUTION_UPDATED("comtaskexecution/UPDATED"),
    COMTASKEXECUTION_DELETED("comtaskexecution/DELETED"),
    ;

    private static final String NAMESPACE = "com/energyict/mdc/device/data/";
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
                .component(DeviceDataService.COMPONENTNAME)
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