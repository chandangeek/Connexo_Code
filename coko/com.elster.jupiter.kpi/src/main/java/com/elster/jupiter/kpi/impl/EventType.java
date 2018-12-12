/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    KPI_TARGET_MISSED("TARGET_MISS");

    private static final String NAMESPACE = "com/elster/jupiter/kpi/";
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
                .component(KpiService.COMPONENT_NAME)
                .category("KPI")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("position", ValueType.INTEGER, "position")
                .withProperty("timestamp", ValueType.LONG, "timestamp");
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }


}
