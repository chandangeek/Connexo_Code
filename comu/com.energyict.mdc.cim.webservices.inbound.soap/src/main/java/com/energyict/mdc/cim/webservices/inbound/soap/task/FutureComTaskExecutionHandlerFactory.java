/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.FutureComTaskExecutionHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_SUBSCRIBER,
                "destination=" + FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION},
        immediate = true)
public class FutureComTaskExecutionHandlerFactory implements MessageHandlerFactory {
    public static final String FUTURE_COM_TASK_EXECUTION_DESTINATION = "FutureComTaskExecTopic";
    public static final String FUTURE_COM_TASK_EXECUTION_SUBSCRIBER = "FutureComTaskExecSubscriber";
    public static final String FUTURE_COM_TASK_EXECUTION_DISPLAYNAME = "Handle future communication task executions for meter readings web service";

    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile ServiceCallService serviceCallService;
    private volatile DeviceService deviceService;
    private volatile PriorityComTaskService priorityComTaskService;

    public FutureComTaskExecutionHandlerFactory() {
        // for OSGi purpose
    }

    @Inject
    public FutureComTaskExecutionHandlerFactory(TaskService taskService, Clock clock, ServiceCallService serviceCallService,
                                                DeviceService deviceService, PriorityComTaskService priorityComTaskService) {
        setTaskService(taskService);
        setClock(clock);
        setServiceCallService(serviceCallService);
        setDeviceService(deviceService);
        setPriorityComTaskService(priorityComTaskService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new FutureComTaskExecutionHandler(clock, serviceCallService, deviceService, priorityComTaskService));
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

    @Reference
    public void setPriorityComTaskService(PriorityComTaskService priorityComTaskService) {
        this.priorityComTaskService = priorityComTaskService;
    }
}
