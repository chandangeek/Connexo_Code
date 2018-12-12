/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved. 
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.pki.SecurityManagementService;

/**
 * Models the different event types that are produced by this bundle.
 */
public enum EventType {

    TRUSTSTORE_DELETED("truststore/DELETED"),
    TRUSTSTORE_VALIDATE_DELETE("truststore/VALIDATE_DELETE"),
    CERTIFICATE_DELETED("certificate/DELETED"),
    CERTIFICATE_VALIDATE_DELETE("certificate/VALIDATE_DELETE"),
    SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE("securityAccessorType/VALIDATE_DELETE"),
    SECURITY_ACCESSOR_TYPE_DELETED("securityAccessorType/DELETED"),
    SECURITY_ACCESSOR_VALIDATE_DELETE("securityAccessor/VALIDATE_DELETE");

    private static final String NAMESPACE = "com/elster/jupiter/pki/";
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
                .component(SecurityManagementService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id");
        this.addCustomProperties(builder).create();
    }

    @TransactionRequired
    void createIfNotExists(EventService eventService) {
        if (!eventService.getEventType(topic()).isPresent()) {
            install(eventService);
        }
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}
