/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;


@Component(name = "com.elster.jupiter.validation.impl",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ValidationServiceImpl.SUBSCRIBER_NAME, "destination=" + ValidationServiceImpl.DESTINATION_NAME},
        immediate = true)
public class DataValidationMessageHandlerFactory implements MessageHandlerFactory {

    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile ServerValidationService validationService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile Clock clock;

    private User user;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new DataValidationTaskExecutor((ValidationServiceImpl) validationService, metrologyConfigurationService, transactionService, validationService.getThesaurus(), threadPrincipalService, clock, getUser()));
    }

    @Reference
    public void setValidationService(ServerValidationService validationService) {
        this.validationService = validationService;
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
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public User getUser() {
        if (user == null) {
            user = userService.findUser(ValidationServiceImpl.VALIDATION_USER).get();
        }
        return user;
    }

}