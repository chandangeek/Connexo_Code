/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {
    VALIDATIONRULESET_CREATED("validationruleset/CREATED", true),
    VALIDATIONRULESET_UPDATED("validationruleset/UPDATED", true),
    VALIDATIONRULESET_DELETED("validationruleset/DELETED", true),
    VALIDATIONRULE_CREATED("validationrule/CREATED", false),
    VALIDATIONRULE_UPDATED("validationrule/UPDATED", false),
    VALIDATIONRULE_DELETED("validationrule/DELETED", false),
    VALIDATIONRULESETVERSION_CREATED("validationrulesetversion/CREATED", false),
    VALIDATIONRULESETVERSION_UPDATED("validationrulesetversion/UPDATED", false),
    VALIDATIONRULESETVERSION_DELETED("validationrulesetversion/DELETED", false),
    VALIDATION_PERFORMED("validation/PERFORMED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(ValidationService.COMPONENTNAME)
                    .category("Validate")
                    .scope("System")
                    .create();
        }
    },
    VALIDATION_RESET("validation/RESET") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(ValidationService.COMPONENTNAME)
                    .category("Validate")
                    .scope("System")
                    .create();
        }
    },
    SUSPECT_VALUE_CREATED("suspect/CREATED") {
        @Override
        public void install(EventService eventService) {
            eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(ValidationService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System")
                    .withProperty("channelsContainerId", ValueType.LONG, "channelsContainerId")
                    .withProperty("suspectedScope", ValueType.MAP, "suspectedScope")
                    .shouldPublish()
                    .create();
        }
    };

    private static final String NAMESPACE = "com/elster/jupiter/validation/";
    private final String topic;
    private boolean hasMRID;

    EventType(String topic) {
        this.topic = topic;
    }

    EventType(String topic, boolean hasMRID) {
        this.topic = topic;
        this.hasMRID = hasMRID;
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
