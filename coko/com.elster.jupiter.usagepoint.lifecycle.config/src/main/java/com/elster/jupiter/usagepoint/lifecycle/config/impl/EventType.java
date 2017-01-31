/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

public enum EventType {

    LIFE_CYCLE_CREATED("CREATED"),
    LIFE_CYCLE_UPDATED("UPDATED"),
    LIFE_CYCLE_BEFORE_DELETE("BEFORE_DELETE"),
    LIFE_CYCLE_DELETED("DELETED"),
    LIFE_CYCLE_STATE_BEFORE_DELETE("state/BEFORE_DELETE"),
    LIFE_CYCLE_STATE_DELETED("state/DELETED"),;

    private static final String NAMESPACE = "com/elster/jupiter/usagepoint/lifecycle/";
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
                .component(UsagePointLifeCycleConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldNotPublish();
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
