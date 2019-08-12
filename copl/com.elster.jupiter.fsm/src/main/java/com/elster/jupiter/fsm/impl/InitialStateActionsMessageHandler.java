/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(name = "com.elster.jupiter.fsm.impl.InitialStateActionsMessageHandler", service = MessageHandler.class, immediate = true)
public class InitialStateActionsMessageHandler implements MessageHandler {

    private volatile JsonService jsonService;
    private volatile EventService eventService;

    public InitialStateActionsMessageHandler() {
        super();
    }

    InitialStateActionsMessageHandler(JsonService jsonService, EventService eventService) {
        super();
        this.setJsonService(jsonService);
        this.setEventService(eventService);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        eventService.postEvent("com/elster/jupiter/fsm/event/state/INIT", messageProperties);
    }
}
