package com.energyict.mdc.issue.datavalidation.impl.event;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

@Component(name = "com.energyict.mdc.issue.datavalidation.DataValidationEventHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {
                        "subscriber=" + DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER,
                        "destination=" + EventService.JUPITER_EVENTS
                      },
           immediate = true)
public class DataValidationEventHandlerFactory implements MessageHandlerFactory {
    
    public static final String AQ_DATA_VALIDATION_EVENT_SUBSCRIBER = "IssueCreationDV";

    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile IssueDataValidationService issueDataValidationService;
    
    @Inject
    public DataValidationEventHandlerFactory(JsonService jsonService, IssueService issueService, NlsService nlsService, MeteringService meteringService, DeviceService deviceService, IssueDataValidationService issueDataValidationService) {
        setJsonService(jsonService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setDeviceService(deviceService);
        setIssueDataValidationService(issueDataValidationService);
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
                bind(DeviceService.class).toInstance(deviceService);
                bind(IssueDataValidationService.class).toInstance(issueDataValidationService);
            }
        });
        return new DataValidationEventHandler(injector);
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
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
    }
    
    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    
    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    @Reference
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
    }
}