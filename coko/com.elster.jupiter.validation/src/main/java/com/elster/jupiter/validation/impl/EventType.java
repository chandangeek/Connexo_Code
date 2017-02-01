/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.validation.ValidationService;

public enum EventType {
    VALIDATIONRULESET_CREATED("validationruleset/CREATED", true),
    VALIDATIONRULESET_UPDATED("validationruleset/UPDATED", true),
    VALIDATIONRULESET_DELETED("validationruleset/DELETED", true),
    VALIDATIONRULE_CREATED("validationrule/CREATED", false),
    VALIDATIONRULE_UPDATED("validationrule/UPDATED", false),
    VALIDATIONRULE_DELETED("validationrule/DELETED", false),
    VALIDATIONRULESETVERSION_CREATED("validationrulesetversion/CREATED", false),
    VALIDATIONRULESETVERSION_UPDATED("validationrulesetversion/UPDATED", false),
    VALIDATIONRULESETVERSION_DELETED("validationrulesetversion/DELETED", false);

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
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(ValidationService.COMPONENTNAME)
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
