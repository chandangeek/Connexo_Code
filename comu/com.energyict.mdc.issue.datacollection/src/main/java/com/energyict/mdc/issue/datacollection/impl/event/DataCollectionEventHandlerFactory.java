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
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
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

@Component(name = "com.energyict.mdc.issue.datacollection.DataCollectionEventHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {"subscriber=" + ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
           immediate = true)
public class DataCollectionEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile CommunicationTaskReportService communicationTaskReportService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile TopologyService topologyService;
    private volatile DeviceService deviceService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework only
    public DataCollectionEventHandlerFactory() {
        super();
    }

    // For testing purposes only
    @Inject
    public DataCollectionEventHandlerFactory(
            JsonService jsonService,
            IssueService issueService,
            MeteringService meteringService,
            CommunicationTaskService communicationTaskService,
            CommunicationTaskReportService communicationTaskReportService,
            ConnectionTaskService connectionTaskService,
            TopologyService topologyService,
            DeviceService deviceService,
            IssueDataCollectionService issueDataCollectionService,
            NlsService nlsService) {
        this();
        setJsonService(jsonService);
        setIssueService(issueService);
        setMeteringService(meteringService);
        setCommunicationTaskService(communicationTaskService);
        setCommunicationTaskReportService(communicationTaskReportService);
        setConnectionTaskService(connectionTaskService);
        setTopologyService(topologyService);
        setDeviceService(deviceService);
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(JsonService.class).toInstance(jsonService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(CommunicationTaskReportService.class).toInstance(communicationTaskReportService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
            }
        });
        return new DataCollectionEventHandler(injector);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueCreationService = issueService.getIssueCreationService();
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public final void setCommunicationTaskReportService(CommunicationTaskReportService communicationTaskReportService) {
        this.communicationTaskReportService = communicationTaskReportService;
    }

    @Reference
    public final void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

}