package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    LOGOUT("LOGOUT");

    private static final String COMPONENT_NAME = "WEB";
    private static final String NAMESPACE = "com/elster/jupiter/http/";
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
                .component(COMPONENT_NAME)
                .category("Session")
                .scope("System")
                .shouldPublish();
        builder.create();
    }

}
