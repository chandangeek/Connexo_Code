package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.comschedules.filter.itimizer.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ SchedulingService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER,
                "destination="+SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION},
        immediate = true)
public class ComScheduleOnDeviceFilterItemizerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile DeviceService deviceService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(ComScheduleOnDeviceFilterItimizer.class).
                init(messageService, jsonService, deviceService);
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
        this.dataModel = ormService.newDataModel("ComSchedulesOnDeviceMessageHandlers", "Message handler for bulk action add/remove comSchedules on device");
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
                bind(DeviceService.class).toInstance(deviceService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }
}
