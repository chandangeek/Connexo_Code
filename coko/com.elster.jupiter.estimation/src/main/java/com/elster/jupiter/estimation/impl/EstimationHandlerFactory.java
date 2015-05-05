package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.estimation.impl.messagehandlerfactory", property = {"subscriber=" + EstimationServiceImpl.SUBSCRIBER_NAME, "destination="+EstimationServiceImpl.DESTINATION_NAME}, service = MessageHandlerFactory.class, immediate = true)
public class EstimationHandlerFactory implements MessageHandlerFactory {

    private volatile IEstimationService estimationService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new EstimationTaskExecutor(estimationService, transactionService, estimationService.getThesaurus()));
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = (IEstimationService) estimationService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

}
