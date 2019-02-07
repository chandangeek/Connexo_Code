/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

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
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.issue.devicelifecycle.DeviceLifecycleEventHandlerFactory",
           service = MessageHandlerFactory.class,
           property = {
                        "subscriber=" + DeviceLifecycleEventHandlerFactory.AQ_DEVICE_LIFECYCLE_EVENT_SUBSCRIBER,
                        "destination=" + EventService.JUPITER_EVENTS
                      },
           immediate = true)
public class DeviceLifecycleEventHandlerFactory implements MessageHandlerFactory {
    
    public static final String AQ_DEVICE_LIFECYCLE_EVENT_SUBSCRIBER = "IssueCreationDL";

    private volatile JsonService jsonService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueService issueService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile IssueDeviceLifecycleService issueDeviceLifecycleService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    
    //for OSGI
    public DeviceLifecycleEventHandlerFactory() {
    }
    
    @Inject
    public DeviceLifecycleEventHandlerFactory(JsonService jsonService, IssueService issueService, NlsService nlsService, MeteringService meteringService, DeviceService deviceService, IssueDeviceLifecycleService issueDeviceLifecycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        setJsonService(jsonService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setDeviceService(deviceService);
        setIssueDeviceLifecycleService(issueDeviceLifecycleService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
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
                bind(IssueDeviceLifecycleService.class).toInstance(issueDeviceLifecycleService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);

            }
        });
        return new DeviceLifecycleEventHandler(injector);
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
        this.thesaurus = nlsService.getThesaurus(IssueDeviceLifecycleService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void setIssueDeviceLifecycleService(IssueDeviceLifecycleService issueDeviceLifecycleService) {
        this.issueDeviceLifecycleService = issueDeviceLifecycleService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }
}