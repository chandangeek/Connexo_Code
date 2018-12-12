/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

public enum EventType {

    UNREGISTERED_FROM_GATEWAY_DELAYED("UNREGISTEREDFROMGATEWAYDELAYED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("deviceIdentifier", ValueType.LONG, "deviceIdentifier").
                    withProperty("ruleId", ValueType.LONG, "ruleId").
                    withProperty("gatewayIdentifier", ValueType.LONG, "gatewayIdentifier");
        }
    };
    private static final String NAMESPACE = "com/energyict/mdc/issue/datacollection/";
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
                .component(IssueDataCollectionService.COMPONENT_NAME)
                .category("Crud")
                .shouldPublish()
                .scope("System");
        addCustomProperties(builder).create();
    }

    public @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
