/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.estimation.impl.messagehandlerfactory", property = {"subscriber=" + EstimationServiceImpl.SUBSCRIBER_NAME, "destination="+EstimationServiceImpl.DESTINATION_NAME}, service = MessageHandlerFactory.class, immediate = true)
public class EstimationHandlerFactory implements MessageHandlerFactory {

    private volatile IEstimationService estimationService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile TimeService timeService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;

    private User user;

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new EstimationTaskExecutor(estimationService, transactionService, meteringService, timeService, threadPrincipalService, getUser()));
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

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public User getUser() {
        if (user == null) {
            user = userService.findUser(EstimationServiceImpl.ESTIMATION_TASKS_USER).get();
        }
        return user;
    }
}
