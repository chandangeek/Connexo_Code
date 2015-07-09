package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.elster.jupiter.export.impl.messagehandlerfactory", property = {"subscriber=" + DataExportServiceImpl.SUBSCRIBER_NAME, "destination="+DataExportServiceImpl.DESTINATION_NAME}, service = MessageHandlerFactory.class, immediate = true)
public class DataExportMessageHandlerFactory implements MessageHandlerFactory {

    private volatile IDataExportService dataExportService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DataExportTaskExecutor(dataExportService, transactionService, dataExportService.getLocalFileWriter(), dataExportService.getThesaurus(), clock));
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = (IDataExportService) dataExportService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
