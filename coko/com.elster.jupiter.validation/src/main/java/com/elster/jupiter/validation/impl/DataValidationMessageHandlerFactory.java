package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
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
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;

    private User user;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DataValidationTaskExecutor(validationService, meteringService, transactionService, validationService.getThesaurus(), threadPrincipalService, getUser()));
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

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {

    }

    public User getUser() {
        if (user == null) {
            user = userService.findUser(ValidationServiceImpl.VALIDATION_USER).get();
        }
        return user;
    }
}
