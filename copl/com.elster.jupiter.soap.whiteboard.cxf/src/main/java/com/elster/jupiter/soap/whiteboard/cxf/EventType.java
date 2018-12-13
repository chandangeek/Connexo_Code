/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.events.EventService;

public enum EventType {

    ENDPOINT_CONFIGURATION_CHANGED("endpoint/CHANGED"),
    WEBSERVICE_REGISTERED("endpoint/REGISTERED"),
    ENDPOINT_CONFIGURATION_VALIDATE_DELETE("endpoint/VALIDATE_DELETE");

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
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(WebServicesService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System").create();
            //                            .shouldPublish();
        }
    }
}
