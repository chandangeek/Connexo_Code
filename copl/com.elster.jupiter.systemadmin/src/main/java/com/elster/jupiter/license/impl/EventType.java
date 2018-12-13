/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    LICENSE_UPDATED("UPDATED");

    private static final String NAMESPACE = "com/elster/jupiter/license/";
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
                .component(LicenseService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("appKey", ValueType.STRING, "applicationKey")
                .withProperty("version", ValueType.LONG, "version");
        builder.create();
    }

}
