/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.parties.PartyService;

public enum EventType {

    PARTY_CREATED("CREATED"),
    PARTY_UPDATED("UPDATED"),
    PARTY_DELETED("DELETED");

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
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(PartyService.COMPONENTNAME)
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
