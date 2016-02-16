package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    METROLOGYCONFIGURATION_CREATED("metrologyconfiguration/CREATED"),
    METROLOGYCONFIGURATION_UPDATED("metrologyconfiguration/UPDATED"),
    METROLOGYCONFIGURATION_DELETED("metrologyconfiguration/DELETED");

    private static final String NAMESPACE = "com/elster/insight/usagepoint/config/";
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
                .component(MetrologyConfigurationService.COMPONENT_NAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("version", ValueType.LONG, "version");
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}