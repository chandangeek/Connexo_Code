/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.WebServiceIssueServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.webservice.issue.impl.event.WebServiceEventHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {
                        "subscriber=" + WebServiceEventHandlerFactory.WEB_SERVICE_EVENT_SUBSCRIBER,
                        "destination=" + EventService.JUPITER_EVENTS
                      },
           immediate = true)
public class WebServiceEventHandlerFactory implements MessageHandlerFactory {
    
    public static final String WEB_SERVICE_EVENT_SUBSCRIBER = "WSIssueCreation";

    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile WebServiceIssueService webServiceIssueService;
    private volatile WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    
    public WebServiceEventHandlerFactory() {
        //for OSGI
    }
    
    @Inject
    public WebServiceEventHandlerFactory(JsonService jsonService,
                                         IssueService issueService,
                                         WebServiceIssueServiceImpl webServiceIssueService,
                                         WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        setJsonService(jsonService);
        setIssueService(issueService);
        setWebServiceIssueService(webServiceIssueService);
        setWebServiceCallOccurrenceService(webServiceCallOccurrenceService);
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
                bind(WebServiceIssueService.class).toInstance(webServiceIssueService);
                bind(WebServiceCallOccurrenceService.class).toInstance(webServiceCallOccurrenceService);
            }
        });
        return injector.getInstance(WebServiceEventHandler.class);
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
    public void setWebServiceIssueService(WebServiceIssueServiceImpl webServiceIssueService) {
        this.webServiceIssueService = webServiceIssueService;
        this.thesaurus = webServiceIssueService.thesaurus();
    }

    @Reference
    public void setWebServiceCallOccurrenceService(WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
    }
}
