/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.sap.CheckConfirmationTimeoutHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_SUBSCRIBER,
                "destination=" + CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_DESTINATION},
        immediate = true)
public class CheckConfirmationTimeoutHandlerFactory  implements MessageHandlerFactory {
    public static final String CHECK_CONFIRMATION_TIMEOUT_TASK_DESTINATION = "CheckConfirmTimeoutTopic";
    public static final String CHECK_CONFIRMATION_TIMEOUT_TASK_SUBSCRIBER = "CheckConfirmTimeoutSubscriber";
    public static final String CHECK_CONFIRMATION_TIMEOUT_TASK_DISPLAYNAME = "Handle check SAP confirmation timeout";

    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile ServiceCallService serviceCallService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    public CheckConfirmationTimeoutHandlerFactory() {
    }

    @Inject
    public CheckConfirmationTimeoutHandlerFactory(TaskService taskService,
                                                  ServiceCallService serviceCallService) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CheckConfirmationTimeoutHandler(clock, serviceCallService, sapCustomPropertySets));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }
}
