package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.firmware.FirmwareService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.firmware.campaigns.handler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class FirmwareCampaignsHandlerFactory implements MessageHandlerFactory {
    public static final String FIRMWARE_CAMPAIGNS_SUBSCRIBER = "FirmwareCampaignsSubscriber";
    private volatile JsonService jsonService;
    private volatile FirmwareService firmwareService;
    private volatile MeteringGroupsService meteringGroupsService;

    // OSGI
    @SuppressWarnings("unused")
    public FirmwareCampaignsHandlerFactory() {}

    @Inject
    public FirmwareCampaignsHandlerFactory(FirmwareService firmwareService) {
        setFirmwareService(firmwareService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new FirmwareCampaignHandler(jsonService, (FirmwareServiceImpl) firmwareService, meteringGroupsService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }
}
