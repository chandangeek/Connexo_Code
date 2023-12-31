/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;

/**
 * Models the different event types that are produced
 * by this finite state machines bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (10:12)
 */
public enum EventType {

    FSM_UPDATED("UPDATED"){
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    },
    TRIGGER_EVENT("event/TRIGGER"),
    CHANGE_EVENT("event/CHANGE"),
    START_BPM("bpm/state/START"),
    STATE_INIT("event/state/INIT"){
        @Override
        EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
            return eventTypeBuilder.shouldPublish();
        }
    }
    ;

    private static final String NAMESPACE = "com/elster/jupiter/fsm/";
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
                .component(FiniteStateMachineServiceImpl.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.shouldPublish(builder).create();
    }

    EventTypeBuilder shouldPublish(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder.shouldNotPublish();
    }

}