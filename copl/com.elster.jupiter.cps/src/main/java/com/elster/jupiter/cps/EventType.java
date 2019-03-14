/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {


    CUSTOM_PROPERTY_SET_REGISTERED("CustomPropertySet/REGISTERED");

    private static final String NAMESPACE = "com/elster/jupiter/cps/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(CustomPropertySetService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldPublish();
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    public void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
