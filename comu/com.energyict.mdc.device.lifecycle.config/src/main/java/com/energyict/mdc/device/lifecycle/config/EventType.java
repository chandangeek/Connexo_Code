/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import static com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService.EVENT_NAMESPACE;

/**
 * Models the different event types that are produced
 * by this device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (15:53)
 */
public enum EventType {

    START_BPM("bpm/START"),
    DEVICE_LIFECYCLE_UPDATE("dlc/update"){
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    TRANSITION_FAILED("transition/FAILED"){
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(DeviceLifeCycleConfigurationService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("device", ValueType.LONG, "device")
                    .withProperty("lifecycle", ValueType.LONG, "lifecycle")
                    .withProperty("transition", ValueType.LONG, "transition")
                    .withProperty("from", ValueType.LONG, "from")
                    .withProperty("to", ValueType.LONG, "to")
                    .withProperty("cause", ValueType.STRING, "cause")
                    .withProperty("modTime", ValueType.LONG, "modTime")
                    .create();
        }
    };


    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return EVENT_NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(DeviceLifeCycleConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.shouldPublish(builder).create();
    }

    EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.shouldNotPublish();
    }
}