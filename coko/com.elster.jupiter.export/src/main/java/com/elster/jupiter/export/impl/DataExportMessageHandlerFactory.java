package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.export.impl.messagehandlerfactory", service = MessageHandlerFactory.class, immediate = true)
public class DataExportMessageHandlerFactory implements MessageHandlerFactory {

    private volatile IDataExportService dataExportService;
    private volatile TaskService taskService;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DataExportTaskExecutor(dataExportService, taskService));
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = (IDataExportService) dataExportService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
