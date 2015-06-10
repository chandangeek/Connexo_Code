package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.connectiontask.reschedule.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+ ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER,
                "destination="+ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION},
        immediate = true)
public class ConnectionTaskRescheduleMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile ConnectionTaskService connectionTaskService;

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.
                getInstance(ConnectionTaskRescheduleMessageHandler.class).
                init(connectionTaskService, jsonService);
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
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("ConnectionTaskMessageHandlers", "Message handler for bulk action on connection tasks");
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
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
            }
        };
    }
}
