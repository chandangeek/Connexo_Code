/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

@Component(name = "com.energyict.mdc.issue.datacollection.DelayedIssueEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ModuleConstants.AQ_DELAYED_ISSUE_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class DelayedIssueEventHandlerFactory implements MessageHandlerFactory {
    private volatile IssueService issueService;
    private volatile JsonService jsonService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile IssueCreationService issueCreationService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework only
    public DelayedIssueEventHandlerFactory() {
        super();
    }

    // For testing purposes only
    @Inject
    public DelayedIssueEventHandlerFactory(IssueService issueService, JsonService jsonService, DeviceService deviceService, TopologyService topologyService, IssueCreationService issueCreationService, CommunicationTaskService communicationTaskService, IssueDataCollectionService issueDataCollectionService, NlsService nlsService, MeteringService meteringService) {
        this();
        setIssueService(issueService);
        setJsonService(jsonService);
        setDeviceService(deviceService);
        setTopologyService(topologyService);
        setCommunicationTaskService(communicationTaskService);
        setIssueDataCollectionService(issueDataCollectionService);
        setMeteringService(meteringService);
        setNlsService(nlsService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
//                bind(JsonService.class).toInstance(jsonService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
//                bind(CommunicationTaskReportService.class).toInstance(communicationTaskReportService);
//                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(JsonService.class).toInstance(jsonService);
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
            }
        });
        return new DelayedIssueEventHandler(injector);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
}
