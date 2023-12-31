package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.communication.completion.handler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CommunicationTestEventHandlerFactory.SUBSCRIBER_NAME, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class CommunicationTestEventHandlerFactory implements MessageHandlerFactory {
    public static final String SUBSCRIBER_NAME = "CommunicationTestEventHandler";
    public static final String SUBSCRIBER_DISPLAYNAME = "Handle events for communication test";

    private volatile JsonService jsonService;
    private volatile DeviceService deviceService;
    private volatile ServiceCallService serviceCallService;
    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new CommunicationTestEventHandler(this.jsonService, this.deviceService, this.serviceCallService, this.meteringService);
    }
}