package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    ESTIMATIONRULESET_CREATED("estimationruleset/CREATED", true),
    ESTIMATIONRULESET_UPDATED("estimationruleset/UPDATED", true),
    ESTIMATIONRULESET_DELETED("estimationruleset/DELETED", true),
    
    ESTIMATIONBLOCK_FAILURE("/estimationblock/FAILURE", false) {
        @Override
        EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
            return super.addCustomProperties(eventTypeBuilder).
                    withProperty("startTime", ValueType.LONG, "startTime").
                    withProperty("endTime", ValueType.LONG, "endTime").
                    withProperty("channelId", ValueType.LONG, "channelId").
                    withProperty("readingType", ValueType.STRING, "readingType");
        }
    }
    ;

    private static final String NAMESPACE = "com/elster/jupiter/estimation/";
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
                .component(EstimationService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id")
                .withProperty("version", ValueType.LONG, "version");
        if (hasMRID) {
            builder.withProperty("MRID", ValueType.STRING, "MRID");
        }
        addCustomProperties(builder).create().save();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}