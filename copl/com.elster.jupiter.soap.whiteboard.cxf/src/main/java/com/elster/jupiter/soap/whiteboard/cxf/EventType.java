/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;

public enum EventType {

    ENDPOINT_CONFIGURATION_CHANGED("endpoint/CHANGED"),
    WEBSERVICE_REGISTERED("endpoint/REGISTERED"),
    ENDPOINT_CONFIGURATION_VALIDATE_DELETE("endpoint/VALIDATE_DELETE"),

    INBOUND_AUTH_FAILURE("fail/in/AUTH_FAILURE") {
        @Override
        protected EventTypeBuilder customize(EventTypeBuilder builder) {
            return builder
                    .shouldPublish()
                    .withProperty("id", ValueType.LONG, "id");
        }
    },
    OUTBOUND_ENDPOINT_NOT_AVAILABLE("fail/out/ENDPOINT_NOT_AVAILABLE") {
        @Override
        protected EventTypeBuilder customize(EventTypeBuilder builder) {
            return builder
                    .shouldPublish()
                    .withProperty("id", ValueType.LONG, "id");
        }
    },
    OUTBOUND_BAD_ACKNOWLEDGEMENT("fail/out/BAD_ACKNOWLEDGEMENT") {
        @Override
        protected EventTypeBuilder customize(EventTypeBuilder builder) {
            return builder
                    .shouldPublish()
                    .withProperty("id", ValueType.LONG, "id");
        }
    },
    OUTBOUND_NO_ACKNOWLEDGEMENT("fail/out/NO_ACKNOWLEDGEMENT") {
        @Override
        protected EventTypeBuilder customize(EventTypeBuilder builder) {
            return builder
                    .shouldPublish()
                    .withProperty("id", ValueType.LONG, "id");
        }
    },
    OUTBOUND_AUTH_FAILURE("fail/out/AUTH_FAILURE") {
        @Override
        protected EventTypeBuilder customize(EventTypeBuilder builder) {
            return builder
                    .shouldPublish()
                    .withProperty("id", ValueType.LONG, "id");
        }
    };

    private static final String NAMESPACE = "com/elster/jupiter/webservices/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    public void installIfNotPresent(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(WebServicesService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System");
            customize(builder).create();
        }
    }

    protected EventTypeBuilder customize(EventTypeBuilder builder) {
        return builder;
    }
}
