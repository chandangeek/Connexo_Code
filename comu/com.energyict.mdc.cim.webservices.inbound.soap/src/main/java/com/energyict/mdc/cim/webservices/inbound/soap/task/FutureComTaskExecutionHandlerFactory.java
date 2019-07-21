/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.CheckConfirmationTimeoutHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_SUBSCRIBER,
                "destination=" + FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION},
        immediate = true)
public class FutureComTaskExecutionHandlerFactory implements MessageHandlerFactory {
    public static final String FUTURE_COM_TASK_EXECUTION_DESTINATION = "FutureComTaskExecTopic";
    public static final String FUTURE_COM_TASK_EXECUTION_SUBSCRIBER = "FutureComTaskExecSubscriber";
    public static final String FUTURE_COM_TASK_EXECUTION_DISPLAYNAME = "Handle future communication task executions";

    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile ServiceCallService serviceCallService;
    private volatile DeviceService deviceService;

    public FutureComTaskExecutionHandlerFactory() {
    }

    @Inject
    public FutureComTaskExecutionHandlerFactory(TaskService taskService, ServiceCallService serviceCallService) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new FutureComTaskExecutionHandler(clock, serviceCallService, deviceService));
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
