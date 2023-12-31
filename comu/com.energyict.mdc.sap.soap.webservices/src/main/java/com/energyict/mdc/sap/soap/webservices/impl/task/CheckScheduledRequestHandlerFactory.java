/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
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

@Component(name = "com.energyict.mdc.sap.CheckScheduledRequestHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_SUBSCRIBER,
                "destination=" + CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_DESTINATION},
        immediate = true)
public class CheckScheduledRequestHandlerFactory implements MessageHandlerFactory {
    public static final String CHECK_SCHEDULED_REQUEST_TASK_DESTINATION = "CheckScheduledReqTopic";
    public static final String CHECK_SCHEDULED_REQUEST_TASK_SUBSCRIBER = "CheckScheduledReqSubscriber";
    public static final String CHECK_SCHEDULED_REQUEST_TASK_DISPLAYNAME = "Handle check scheduled SAP requests";

    private volatile TaskService taskService;
    private volatile ServiceCallService serviceCallService;
    private volatile Clock clock;

    public CheckScheduledRequestHandlerFactory() {
    }

    @Inject
    public CheckScheduledRequestHandlerFactory(TaskService taskService,
                                               ServiceCallService serviceCallService, Clock clock) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
        setClock(clock);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CheckScheduledRequestHandler(serviceCallService, clock));
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
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }
}
