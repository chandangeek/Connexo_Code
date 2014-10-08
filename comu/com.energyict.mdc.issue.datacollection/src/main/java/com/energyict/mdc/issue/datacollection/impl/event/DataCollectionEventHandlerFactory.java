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
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
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
    private volatile ConnectionTaskService connectionTaskService;
    private volatile DeviceService deviceService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile Thesaurus thesaurus;

    @Inject
    public DataCollectionEventHandlerFactory(
            JsonService jsonService,
            IssueCreationService issueCreationService,
            IssueService issueService,
            MeteringService meteringService,
            CommunicationTaskService communicationTaskService,
            ConnectionTaskService connectionTaskService,
            DeviceService deviceService,
            IssueDataCollectionService issueDataCollectionService,
            NlsService nlsService) {
        setJsonService(jsonService);
        setIssueCreationService(issueCreationService);
        setIssueService(issueService);
        setMeteringService(meteringService);
        setCommunicationTaskService(communicationTaskService);
        setConnectionTaskService(connectionTaskService);
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
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
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
    public final void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public final void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }
    @Reference
    public final void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
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
