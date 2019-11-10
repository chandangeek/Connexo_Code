/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.sap.CheckStatusChangeCancellationHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_SUBSCRIBER,
                "destination=" + CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_DESTINATION},
        immediate = true)
public class CheckStatusChangeCancellationHandlerFactory implements MessageHandlerFactory {
    public static final String CHECK_STATUS_CHANGE_CANCELLATION_TASK_DESTINATION = "StatusChgCancelTopic";
    public static final String CHECK_STATUS_CHANGE_CANCELLATION_TASK_SUBSCRIBER = "StatusChgCancelSubscriber";
    public static final String CHECK_STATUS_CHANGE_CANCELLATION_TASK_DISPLAYNAME = "Check status change cancellation";
    public static final int CHECK_STATUS_CHANGE_CANCELLATION_TASK_RETRY_DELAY = 60;

    private volatile TaskService taskService;
    private volatile ServiceCallService serviceCallService;
    private volatile Clock clock;
    private volatile WebServiceActivator webServiceActivator;

    public CheckStatusChangeCancellationHandlerFactory() {
        // for OSGI purpose
    }

    @Inject
    public CheckStatusChangeCancellationHandlerFactory(TaskService taskService, ServiceCallService serviceCallService,
                                                       Clock clock, WebServiceActivator webServiceActivator) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
        setClock(clock);
        setWebServiceActivator(webServiceActivator);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CheckStatusChangeCancellationHandler(serviceCallService, clock, webServiceActivator));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
