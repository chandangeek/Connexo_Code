/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationServiceImpl;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.elster.jupiter.validation.impl.kpi", service = MessageHandlerFactory.class, property = {"subscriber=" + DataValidationKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, "destination=" + DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION}, immediate = true)
public class DataValidationKpiCalculatorHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "ValKpiCalcTopic";
    public static final String TASK_SUBSCRIBER = "ValKpiCalc";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate validation kpi's";

    private volatile TaskService taskService;
    private volatile DataValidationKpiService dataValidationKpiService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile ValidationService validationService;
    private volatile Clock clock;
    private User user;

    public DataValidationKpiCalculatorHandlerFactory() {
        super();
    }

    @Inject
    public DataValidationKpiCalculatorHandlerFactory(TaskService taskService, DataValidationKpiService dataValidationKpiService,
                                                     TransactionService transactionService, ThreadPrincipalService threadPrincipalService,
                                                     UserService userService, User user, Clock clock) {
        this();
        this.setTaskService(taskService);
        this.setDataValidationKpiService(dataValidationKpiService);
        this.setClock(clock);
        this.setUserService(userService);
        this.setTransactionService(transactionService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.user = user;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataManagementKpiCalculatorHandler(
                        dataValidationKpiService,
                        transactionService, threadPrincipalService, validationService, clock,
                        getUser()));
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDataValidationKpiService(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public User getUser() {
        if (user == null) {
            user = userService.findUser(ValidationServiceImpl.VALIDATION_USER).get();
        }
        return user;
    }
}
