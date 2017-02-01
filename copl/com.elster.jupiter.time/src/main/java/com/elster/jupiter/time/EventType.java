/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EventType {
    CATEGORY_USAGE_DELETED("relativeperiodcategoryusage/DELETED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("relativePeriodId", ValueType.LONG, "relativePeriodId");
            eventTypeBuilder.withProperty("relativePeriodCategoryId", ValueType.LONG, "relativePeriodCategoryId");
            return eventTypeBuilder;
        }
    },
    RELATIVE_PERIOD_CREATED("relativeperiod/CREATED", withId()),
    RELATIVE_PERIOD_UPDATED("relativeperiod/UPDATED", withId()),
    RELATIVE_PERIOD_DELETED("relativeperiod/DELETED", withId()),
    RELATIVE_PERIOD_CATEGORY_CREATED("relativeperiodcategory/CREATED", withId()),
    RELATIVE_PERIOD_CATEGORY_UPDATED("relativeperiodcategory/UPDATED", withId()),
    RELATIVE_PERIOD_CATEGORY_DELETED("relativeperiodcategory/DELETED", withId()),;

    private static PropertyAdder withId() {
        return b -> b.withProperty("id", ValueType.LONG, "id");
    }

    private static final String NAMESPACE = "com/elster/jupiter/time/";
    private final String topic;
    private final List<PropertyAdder> propertyAdders = new ArrayList<>();

    EventType(String topic, PropertyAdder... adders) {
        this.topic = topic;
        this.propertyAdders.addAll(Arrays.asList(adders));
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(TimeService.COMPONENT_NAME)
                .category("Crud")
                .scope("System");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    public void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        propertyAdders.forEach(adder -> adder.addProperty(eventTypeBuilder));
        return eventTypeBuilder;
    }

    private interface PropertyAdder {
        void addProperty(EventTypeBuilder builder);
    }
}
