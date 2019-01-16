/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    TRANSITION_FAILED("transition/FAILED"){
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(EstimationService.COMPONENTNAME)
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
    }
    ;

    private static final String NAMESPACE = "com/energyict/mdc/device/lifecycle/";
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
                .component(EstimationService.COMPONENTNAME)
                .category("Crud")
                .scope("System");
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}