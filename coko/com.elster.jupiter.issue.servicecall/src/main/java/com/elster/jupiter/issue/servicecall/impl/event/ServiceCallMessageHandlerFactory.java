/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "ServiceCallMessageHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {
                        "subscriber=" + ServiceCallMessageHandlerFactory.AQ_SERVICE_CALL_EVENT_SUBSCRIBER,
                        "destination=" + ServiceCallService.SERVICE_CALLS_ISSUE_DESTINATION_NAME
                      },
           immediate = true)
public class ServiceCallMessageHandlerFactory implements MessageHandlerFactory {

    public static final String AQ_SERVICE_CALL_EVENT_SUBSCRIBER = ServiceCallService.SERVICE_CALLS_ISSUE_SUBSCRIBER_NAME;

    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile ServiceCallIssueService issueServiceCallService;
    private volatile ServiceCallService serviceCallService;

    //for OSGI
    public ServiceCallMessageHandlerFactory() {
    }

    @Inject
    public ServiceCallMessageHandlerFactory(JsonService jsonService, IssueService issueService, NlsService nlsService,
                                            MeteringService meteringService, ServiceCallIssueService issueServiceCallService,
                                            ServiceCallService serviceCallService) {
        setJsonService(jsonService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setIssueServiceCallService(issueServiceCallService);
        setServiceCallService(serviceCallService);
    }
    
    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ServiceCallIssueService.class).toInstance(issueServiceCallService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
            }
        });
        return new ServiceCallMessageHandler(injector);
    }
    
    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    
    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueCreationService = issueService.getIssueCreationService();
    }
    
    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN);
    }
    
    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setIssueServiceCallService(ServiceCallIssueService issueServiceCallService) {
        this.issueServiceCallService = issueServiceCallService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }
}