/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.engine.config.EngineConfigurationService;

/**
 * Models the different event types that are produced by this "engine configuration bundle".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (16:23)
 */
public enum EventType {

    COMPORTPOOL_CREATED("comportpool/CREATED"),
    COMPORTPOOL_UPDATED("comportpool/UPDATED"),
    COMPORTPOOL_VALIDATE_DELETE("comportpool/VALIDATE_DELETE"),
    COMPORTPOOL_DELETED("comportpool/DELETED");

    private static final String NAMESPACE = "com/energyict/mdc/engine/config/";
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
                .component(EngineConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder).create();
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        eventTypeBuilder.withProperty("id", ValueType.LONG, "id");
        return eventTypeBuilder;
    }

}