/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.communicationtask.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER,
                "destination="+CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION},
        immediate = true)
public class CommunicationTaskBatchMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile CommunicationTaskService communicationTaskService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(CommunicationTaskBatchMessageHandler.class).
                init(communicationTaskService, jsonService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setConnectionTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
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
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
            }
        };
    }
}
