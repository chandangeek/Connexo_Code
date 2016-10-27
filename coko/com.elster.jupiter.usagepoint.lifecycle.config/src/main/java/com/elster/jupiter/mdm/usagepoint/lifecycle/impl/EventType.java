package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    LIFE_CYCLE_CREATED("lifecycle/CREATED"),
    LIFE_CYCLE_UPDATED("lifecycle/UPDATED"),
    LIFE_CYCLE_BEFORE_DELETE("lifecycle/BEFORE_DELETE"),
    LIFE_CYCLE_DELETED("lifecycle/DELETED"),;

    private static final String NAMESPACE = "com/elster/jupiter/mdm/usagepoint";
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
                .component(UsagePointLifeCycleService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .shouldNotPublish();
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
