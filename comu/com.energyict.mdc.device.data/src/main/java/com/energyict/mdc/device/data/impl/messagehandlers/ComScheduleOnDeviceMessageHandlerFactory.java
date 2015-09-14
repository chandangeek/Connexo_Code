package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.comschedule.device.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ SchedulingService.COM_SCHEDULER_QUEUE_SUBSCRIBER,
                "destination="+SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION},
        immediate = true)
public class ComScheduleOnDeviceMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile DeviceService deviceService;
    private volatile SchedulingService schedulingService;
    private volatile Thesaurus thesaurus;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(ComScheduleOnDeviceMessageHandler.class).
                init(deviceService, schedulingService, jsonService, thesaurus);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    private void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("ConnectionTaskMessageHandlers", "Message handler for bulk action on connection tasks");
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }


    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(JsonService.class).toInstance(jsonService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(DeviceService.class).toInstance(deviceService);
            }
        };
    }
}
