/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.device.topology.TopologyService;

public enum EventType {

    UNREGISTERED_FROM_GATEWAY("UNREGISTEREDFROMGATEWAY") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifier", ValueType.LONG, "deviceIdentifier").
                    withProperty("gatewayIdentifier", ValueType.LONG, "gatewayIdentifier");
        }
    },
    REGISTERED_TO_GATEWAY("REGISTEREDTOGATEWAY") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifier", ValueType.LONG, "deviceIdentifier");
        }
    };
    private static final String NAMESPACE = "com/energyict/mdc/topology/";
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
                .component(TopologyService.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
         addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
