package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    METROLOGYCONFIGURATION_CREATED("metrologyconfiguration/CREATED"),
    METROLOGYCONFIGURATION_UPDATED("metrologyconfiguration/UPDATED"),
    METROLOGYCONFIGURATION_DELETED("metrologyconfiguration/DELETED");

    private static final String NAMESPACE = "com/elster/insight/usagepoint/config/";
    private final String topic;
    private boolean hasMRID;

    EventType(String topic) {
        this.topic = topic;
    }

    EventType(String topic, boolean mRID) {
        this.topic = topic;
        this.hasMRID = mRID;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(UsagePointConfigurationService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("version", ValueType.LONG, "version");
        if (hasMRID) {
            builder.withProperty("MRID", ValueType.STRING, "MRID");
        }
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}
