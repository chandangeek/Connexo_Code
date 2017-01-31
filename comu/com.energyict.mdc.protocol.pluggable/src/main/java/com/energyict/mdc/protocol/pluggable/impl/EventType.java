/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

/**
 * Models the different event types that are produced by this Protocol Pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (15:30)
 */
public enum EventType {
    CONNECTIONTYPE_CREATED("connectiontype/CREATED"),
    CONNECTIONTYPE_UPDATED("connectiontype/UPDATED"),
    CONNECTIONTYPE_DELETED("connectiontype/DELETED"),
    DEVICEPROTOCOL_CREATED("deviceprotocol/CREATED"),
    DEVICEPROTOCOL_UPDATED("deviceprotocol/UPDATED"),
    DEVICEPROTOCOL_DELETED("deviceprotocol/DELETED"),
    INBOUNDDEVICEPROTOCOL_CREATED("inbounddeviceprotocol/CREATED"),
    INBOUNDDEVICEPROTOCOL_UPDATED("inbounddeviceprotocol/UPDATED"),
    INBOUNDDEVICEPROTOCOL_DELETED("inbounddeviceprotocol/DELETED");

    private static final String NAMESPACE = "com/energyict/mdc/protocol/pluggable/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(ProtocolPluggableService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id");
        this.addCustomProperties(builder).create();
    }

    private EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}