/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.devicemessage.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ DeviceMessageService.DEVICE_MESSAGE_QUEUE_SUBSCRIBER,
                "destination="+DeviceMessageService.DEVICE_MESSAGE_QUEUE_DESTINATION},
        immediate = true)
public class CreateDeviceMessageMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile DeviceService deviceService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(CreateDeviceMessageMessageHandler.class).
                init(jsonService, deviceService);
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
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("CommunicationTaskMessageHandlers", "Message handler for bulk action on communication tasks");
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }


    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(DeviceService.class).toInstance(deviceService);
            }
        };
    }
}
