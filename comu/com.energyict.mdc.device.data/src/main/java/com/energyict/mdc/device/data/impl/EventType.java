/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.data.DeviceDataServices;

/**
 * Models the different event types that are produced by this "device data bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum EventType {

    CONNECTIONTASK_CREATED("connectiontask/CREATED"),
    CONNECTIONTASK_UPDATED("connectiontask/UPDATED"),
    CONNECTIONTASK_SETASDEFAULT("connectiontask/SETASDEFAULT"),
    CONNECTIONTASK_CLEARDEFAULT("connectiontask/CLEARDEFAULT"),
    CONNECTIONTASK_CHECK_ALL_ACTIVE("connectiontask/CHECKALLACTIVE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            eventTypeBuilder.withProperty("partialConnectionTaskId", ValueType.LONG, "partialConnectionTaskId");
            eventTypeBuilder.shouldPublish();
            return eventTypeBuilder;
        }
    },
    CONNECTIONTASK_DELETED("connectiontask/DELETED"),
    DEVICE_CREATED("device/CREATED"),
    DEVICE_DELETED("device/DELETED"),
    DEVICE_UPDATED("device/UPDATED"),
    DEVICE_BEFORE_DELETE("device/BEFORE_DELETE"),
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
    COMTASKEXECUTION_VALIDATE_OBSOLETE("comtaskexecution/VALIDATE_OBSOLETE"),
    COMTASKEXECUTION_UPDATED("comtaskexecution/UPDATED"),
    COMTASKEXECUTION_RANGE_OBSOLETE("comtaskexecution/RANGE_OBSOLETE"),
    COMTASKEXECUTION_DELETED("comtaskexecution/DELETED"),
    COMSCHEDULE_UPDATED("comschedule/UPDATED") {
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.withProperty("minId", ValueType.LONG, "minId")
                    .withProperty("maxId", ValueType.LONG, "maxId").withProperty("comScheduleId", ValueType.LONG, "comScheduleId");
        }
    },
    COMTASKEXECUTIONTRIGGER_CREATED("comtaskexecutiontrigger/CREATED"),
    COMTASKEXECUTIONTRIGGER_UPDATED("comtaskexecutiontrigger/UPDATED"),
    COMTASKEXECUTIONTRIGGER_DELETED("comtaskexecutiontrigger/DELETED"),
    DEVICEMESSAGE_CREATED("deviceMessage/CREATED"),
    DEVICEMESSAGE_UPDATED("deviceMessage/UPDATED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("oldDeviceMessageStatus", ValueType.INTEGER, "oldDeviceMessageStatus")
                    .withProperty("oldReleaseDate", ValueType.LONG, "oldReleaseDate");
            return eventTypeBuilder;
        }
    },
    DEVICEMESSAGE_DELETED("deviceMessage/DELETED"),
    FIRMWARE_COMTASKEXECUTION_STARTED("firmwarecomtaskexecution/STARTED"),
    FIRMWARE_COMTASKEXECUTION_COMPLETED("firmwarecomtaskexecution/COMPLETED"),
    FIRMWARE_COMTASKEXECUTION_FAILED("firmwarecomtaskexecution/FAILED"),
    ACTIVATED_BREAKER_STATUS_CREATED("activatedbreakerstatus/CREATED"),
    ACTIVATED_BREAKER_STATUS_UPDATED("activatedbreakerstatus/UPDATED"),
    ACTIVATED_BREAKER_STATUS_DELETED("activatedbreakerstatus/DELETED"),
    RESTARTED_METERACTIVATION("meteractivation/RESTARTED")
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
                .component(DeviceDataServices.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
    }

}