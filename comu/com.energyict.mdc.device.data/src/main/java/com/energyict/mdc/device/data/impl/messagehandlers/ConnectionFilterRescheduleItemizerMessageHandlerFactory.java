package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.FilterFactory;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory for message handlers to itemize connection task for the purpose of rescheduling the tasks
 * Itemize means convert a filter into an exhaustive set of connection tasks
 */
@Component(name = "com.energyict.mdc.connectiontask.filter.itimizer.reschedule.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER,
                "destination="+ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION},
        immediate = true)
public class ConnectionFilterRescheduleItemizerMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MessageService messageService;
    private volatile FilterFactory filterFactory;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(ConnectionFilterRescheduleItemizerMessageHandler.class).
                init(connectionTaskService, filterFactory, messageService, jsonService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("ConnectionTaskRescheduleMessageHandlers", "Message handler for bulk rescheduling on connection tasks");
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

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }


    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }
}
