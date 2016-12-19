package com.energyict.mdc.device.alarms.impl.event;

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
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

@Component(name = "com.energyict.mdc.device.alarms.DeviceAlarmEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ModuleConstants.AQ_DEVICE_ALARM_EVENT_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class DeviceAlarmEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework only
    public DeviceAlarmEventHandlerFactory() {
        super();
    }

    // For testing purposes only
    @Inject
    public DeviceAlarmEventHandlerFactory(
            JsonService jsonService,
            IssueService issueService,
            MeteringService meteringService,
            DeviceService deviceService,
            DeviceAlarmService deviceAlarmService,
            NlsService nlsService) {
        this();
        setJsonService(jsonService);
        setIssueService(issueService);
        setMeteringService(meteringService);
        setDeviceService(deviceService);
        setDeviceAlarmService(deviceAlarmService);
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
                bind(DeviceService.class).toInstance(deviceService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueService.class).toInstance(issueService);
                bind(DeviceAlarmService.class).toInstance(deviceAlarmService);
            }
        });
        return new DeviceAlarmEventHandler(injector);
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
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

}