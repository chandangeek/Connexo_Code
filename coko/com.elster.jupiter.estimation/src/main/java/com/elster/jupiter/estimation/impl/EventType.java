package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    ESTIMATIONRULESET_CREATED("estimationruleset/CREATED"),
    ESTIMATIONRULESET_UPDATED("estimationruleset/UPDATED"),
    ESTIMATIONRULESET_DELETED("estimationruleset/DELETED"),
    
    ESTIMATIONBLOCK_FAILURE("estimationblock/FAILURE") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(EstimationService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("startTime", ValueType.LONG, "startTime")
                    .withProperty("endTime", ValueType.LONG, "endTime")
                    .withProperty("channelId", ValueType.LONG, "channelId")
                    .withProperty("readingType", ValueType.STRING, "readingType")
                    .create().save();
        }
    }
    ;

    private static final String NAMESPACE = "com/elster/jupiter/estimation/";
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
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("version", ValueType.LONG, "version")
                .withProperty("MRID", ValueType.STRING, "MRID");
        addCustomProperties(builder).create().save();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}