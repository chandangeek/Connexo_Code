/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.tasks.TaskService;

/**
 * Models the different event types that are produced by this task bundle.
 */
public enum EventType {

    COMTASK_DELETED("comtask/DELETED"),
    COMTASK_VALIDATE_DELETE("comtask/VALIDATE_DELETE"),
    COMTASK_CREATED("comtask/CREATED"),
    COMTASK_UPDATED("comtask/UPDATED");

    private static final String NAMESPACE = "com/energyict/mdc/tasks/";
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
                .component(TaskService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}