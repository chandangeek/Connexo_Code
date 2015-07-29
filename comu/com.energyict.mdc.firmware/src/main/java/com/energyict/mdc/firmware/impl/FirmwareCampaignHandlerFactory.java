package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.tasks.TaskService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.firmware.campaigns.handler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + FirmwareCampaignHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class FirmwareCampaignHandlerFactory implements MessageHandlerFactory {
    public static final String FIRMWARE_CAMPAIGNS_SUBSCRIBER = "FirmwareCampaignsSubscriber";
    private volatile JsonService jsonService;
    private volatile FirmwareService firmwareService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private volatile EventService eventService;
    private volatile TaskService taskService;

    // OSGI
    @SuppressWarnings("unused")
    public FirmwareCampaignHandlerFactory() {
    }

    @Inject
    public FirmwareCampaignHandlerFactory(FirmwareService firmwareService) {
        setFirmwareService(firmwareService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        FirmwareCampaignHandlerContext handlerContext = new FirmwareCampaignHandlerContext((FirmwareServiceImpl) firmwareService, meteringGroupsService, deviceService, clock, eventService, taskService);
        return new FirmwareCampaignHandler(jsonService, handlerContext);
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

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTaskService(TaskService taskService){
        this.taskService = taskService;
    }
}
