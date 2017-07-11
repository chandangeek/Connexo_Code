/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceMessageService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.devicegroup.bulk.devicemessage.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ DeviceMessageService.BULK_DEVICE_MESSAGE_QUEUE_SUBSCRIBER,
                "destination="+DeviceMessageService.BULK_DEVICE_MESSAGE_QUEUE_DESTINATION},
        immediate = true)
public class DeviceMessageItemizerHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile MeteringGroupsService meteringGroupService;
    private volatile Clock clock;
    private volatile MessageService messageService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(DeviceMessageItemizerMessageHandler.class).
                init(jsonService, meteringGroupService, clock, messageService);
    }

    @Reference
    public void setMeteringGroupService(MeteringGroupsService meteringGroupService) {
        this.meteringGroupService = meteringGroupService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("CommunicationTaskMessageHandlers", "Message handler for bulk action on communication tasks");
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
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
                bind(MeteringGroupsService.class).toInstance(meteringGroupService);
                bind(Clock.class).toInstance(clock);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }
}
