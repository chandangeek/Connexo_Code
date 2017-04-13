/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.processes.keyrenewal.api.CompletionOptionsMessageHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_TASK_SUBSCRIBER,
                "destination=" + CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION},
        immediate = true)
public class CompletionOptionsMessageHandlerFactory implements MessageHandlerFactory {

    public static final String COMPLETION_OPTIONS_DESTINATION = "PkrComplOptTopic";
    public static final String COMPLETION_OPTIONS_TASK_SUBSCRIBER = "PkrComplOptSubscriber";
    public static final String COMPLETION_OPTIONS_TASK_SUBSCRIBER_DISPLAYNAME = "Handle key renewal response";

    private volatile JsonService jsonService;
    private volatile ServiceCallService serviceCallService;
    private volatile BpmService bpmService;
    private volatile Thesaurus thesaurus;

    public CompletionOptionsMessageHandlerFactory() {
    }

    @Inject
    public CompletionOptionsMessageHandlerFactory(
            JsonService jsonService,
            ServiceCallService serviceCallService,
            NlsService nlsService,
            BpmService bpmService) {
        setJsonService(jsonService);
        setServiceCallService(serviceCallService);
        setNlsService(nlsService);
        setBpmService(bpmService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new CompletionOptionsHandler(jsonService, serviceCallService, thesaurus, bpmService);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(KeyRenewalApplication.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

}