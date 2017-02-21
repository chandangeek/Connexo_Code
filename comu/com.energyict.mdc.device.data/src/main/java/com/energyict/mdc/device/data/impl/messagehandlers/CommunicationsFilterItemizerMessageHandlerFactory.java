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
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.communicationtask.filter.itimizer.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER,
                "destination="+CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION},
        immediate = true)
public class CommunicationsFilterItemizerMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MessageService messageService;
    private volatile SchedulingService schedulingService;
    private volatile TaskService taskService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(CommunicationFilterItimizerMessageHandler.class).
                init(communicationTaskService, deviceConfigurationService, meteringGroupsService, messageService,
                        jsonService, schedulingService, taskService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("CommunicationTaskMessageHandlers", "Message handler for bulk action on communication tasks");
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
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
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }
}
