package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    VALIDATIONRULESET_CREATED("validationruleset/CREATED", true),
    VALIDATIONRULESET_UPDATED("validationruleset/UPDATED", true),
    VALIDATIONRULESET_DELETED("validationruleset/DELETED", true);

    private static final String NAMESPACE = "com/elster/jupiter/validation/";
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
    public void install() {
        EventTypeBuilder builder = Bus.getEventService().buildEventTypeWithTopic(topic())
                .name(name())
                .component(Bus.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .shouldPublish()
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
