package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    SERVICELOCATION_CREATED("servicelocation/CREATED", true),
    SERVICELOCATION_UPDATED("servicelocation/UPDATED", true),
    SERVICELOCATION_DELETED("servicelocation/DELETED", true),
    USAGEPOINT_CREATED("usagepoint/CREATED", true),
    USAGEPOINT_UPDATED("usagepoint/UPDATED", true),
    USAGEPOINT_DELETED("usagepoint/DELETED", true),
    CHANNEL_CREATED("channel/CREATED"),
    CHANNEL_UPDATED("channel/UPDATED"),
    CHANNEL_DELETED("channel/DELETED"),
    METER_CREATED("meter/CREATED", true),
    METER_UPDATED("meter/UPDATED", true),
    METER_DELETED("meter/DELETED", true);

    private static final String NAMESPACE = "com/elster/jupiter/metering/";
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
