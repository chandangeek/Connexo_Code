/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.impl.ServerDataQualityKpiService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationServiceImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(
        name = "com.elster.jupiter.dataquality.kpi.messagehandler",
        service = MessageHandlerFactory.class,
        property = {
                "subscriber=" + DataQualityKpiCalculatorHandlerFactory.TASK_SUBSCRIBER,
                "destination=" + DataQualityKpiCalculatorHandlerFactory.TASK_DESTINATION
        },
        immediate = true
)
public class DataQualityKpiCalculatorHandlerFactory implements MessageHandlerFactory, DataQualityServiceProvider {

    public static final String TASK_DESTINATION = "DataQualityKpiCalcTopic";
    public static final String TASK_SUBSCRIBER = "DataQualityKpiCalc";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Calculate data quality kpi's";

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile ServerDataQualityKpiService dataQualityKpiService;
    private volatile TaskService taskService;
    private volatile UserService userService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile Clock clock;
    private User user;

    public DataQualityKpiCalculatorHandlerFactory() {
        super();
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(
                new DataQualityKpiCalculatorHandler(this, getUser()));
    }

    public User getUser() {
        if (user == null) {
            user = userService.findUser(ValidationServiceImpl.VALIDATION_USER).get(); // for the moment we can still use VALIDATION user here
        }
        return user;
    }

    @Override
    public ThreadPrincipalService threadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public TransactionService transactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public DataQualityKpiService dataQualityKpiService() {
        return dataQualityKpiService;
    }

    @Override
    public DataModel dataModel() {
        return dataQualityKpiService.getDataModel();
    }

    @Reference
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = (ServerDataQualityKpiService) dataQualityKpiService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ValidationService validationService() {
        return validationService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public EstimationService estimationService() {
        return estimationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
