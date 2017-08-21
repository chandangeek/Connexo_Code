/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.issue.datacollection.DelayedIssueEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ModuleConstants.AQ_DELAYED_ISSUE_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class DelayedIssueEventHandlerFactory implements MessageHandlerFactory {
    private volatile IssueService issueService;


    // For OSGi framework only
    public DelayedIssueEventHandlerFactory() {
        super();
    }

    // For testing purposes only
    @Inject
    public DelayedIssueEventHandlerFactory(IssueService issueService) {
        this();
        setIssueService(issueService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
//                bind(Thesaurus.class).toInstance(thesaurus);
//                bind(MessageInterpolator.class).toInstance(thesaurus);
//                bind(JsonService.class).toInstance(jsonService);
//                bind(MeteringService.class).toInstance(meteringService);
//                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
//                bind(CommunicationTaskReportService.class).toInstance(communicationTaskReportService);
//                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
//                bind(TopologyService.class).toInstance(topologyService);
//                bind(DeviceService.class).toInstance(deviceService);
//                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
//                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
            }
        });
        return new DelayedIssueEventHandler(injector);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
}
