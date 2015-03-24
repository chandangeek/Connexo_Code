package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


@Component(name = "com.elster.jupiter.validation.impl",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ValidationServiceImpl.SUBSCRIBER_NAME, "destination="+ ValidationServiceImpl.DESTINATION_NAME},
        immediate = true)
public class DataValidationMessageHandlerFactory implements MessageHandlerFactory {

    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;

    @Override
    public MessageHandler newMessageHandler() {

        return taskService.createMessageHandler(new DataValidationTaskExecutor(validationService, meteringService, transactionService,validationService.getThesaurus()));

    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = (ValidationService) validationService;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {

    }

}
