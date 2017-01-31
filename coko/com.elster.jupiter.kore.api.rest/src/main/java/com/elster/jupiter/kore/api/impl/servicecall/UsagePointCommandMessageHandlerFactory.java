/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandMessageHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + UsagePointCommandMessageHandlerFactory.SUBSCRIBER_NAME, "destination=CommandCallback"},
        immediate = true)
public class UsagePointCommandMessageHandlerFactory implements MessageHandlerFactory {

    public static final String SUBSCRIBER_NAME = "CommandCallback";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle usage point commands";

    private volatile ServiceCallService serviceCallService;
    private volatile JsonService jsonService;

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new UsagePointCommandMessageHandler(serviceCallService, jsonService);
    }
}
