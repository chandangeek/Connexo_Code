package com.energyict.mdc.device.data.impl.ami.eventhandler;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.connectiontask.completion.handler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=ConnectionTaskHandler", "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class ConnectionTaskEventHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DeviceService deviceService;
    private volatile ServiceCallService serviceCallService;

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
        return new ConnectionTaskEventHandler(this.jsonService, this.deviceService, this.serviceCallService);
    }

}