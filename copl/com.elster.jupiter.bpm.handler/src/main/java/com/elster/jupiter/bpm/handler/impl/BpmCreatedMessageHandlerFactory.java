/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.handler.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.elster.jupiter.bpm.handler", service = MessageHandlerFactory.class, property = {"subscriber=BpmQueueSubsc", "destination=BpmQueueDest"}, immediate = true)
public class BpmCreatedMessageHandlerFactory implements MessageHandlerFactory {

    private volatile JsonService jsonService;
    private volatile BpmService bpmService;
    private volatile Clock clock;
    private volatile HttpAuthenticationService httpAuthenticationService;

    public BpmCreatedMessageHandlerFactory(){
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new BpmCreatedMessageHandler(jsonService, bpmService.getBpmServer(), httpAuthenticationService);
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setHttpAuthenticationService(HttpAuthenticationService httpAuthenticationService) {
        this.httpAuthenticationService = httpAuthenticationService;
    }


    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

}