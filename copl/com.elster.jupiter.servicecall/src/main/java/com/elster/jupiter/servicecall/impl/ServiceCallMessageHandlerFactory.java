/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 3/7/16.
 */
@Component(name = "com.elster.jupiter.servicecalls.messagehandlerfactory",
        property = {"subscriber=" + ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME, "destination=" + ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME},
        service = MessageHandlerFactory.class,
        immediate = true)
public class ServiceCallMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ServiceCallMessageHandler(jsonService, (IServiceCallService) serviceCallService, thesaurus);
    }
}
