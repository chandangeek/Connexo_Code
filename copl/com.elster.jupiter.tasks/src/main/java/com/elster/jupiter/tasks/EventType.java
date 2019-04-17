/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

import static com.elster.jupiter.tasks.TaskService.EVENT_NAMESPACE;


/**
 * Models the different event types that are produced
 * by this device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (15:53)
 */
public enum EventType {

    TASK_OCCURRENCE_FAILED("taskoccurrence/FAILED"){
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(TaskService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("taskOccurrenceId", ValueType.LONG, "taskOccurrenceId")
                    .withProperty("errorMessage", ValueType.STRING, "errorMessage")
                    .withProperty("failureTime", ValueType.LONG, "failureTime")
                    .create();
        };
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
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
                .component(TaskService.COMPONENTNAME)
                .category("Crud")
                .scope("System");
        this.shouldPublish(builder).create();
    }


    @TransactionRequired
    public void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.shouldPublish();
    }
}