/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.EndDeviceControlsCancellationHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + EndDeviceControlsCancellationHandlerFactory.END_DEVICE_CONTROLS_CANCELLATION_TASK_SUBSCRIBER,
                "destination=" + EndDeviceControlsCancellationHandlerFactory.END_DEVICE_CONTROLS_CANCELLATION_TASK_DESTINATION},
        immediate = true)
public class EndDeviceControlsCancellationHandlerFactory implements MessageHandlerFactory {
    public static final String END_DEVICE_CONTROLS_CANCELLATION_TASK_DESTINATION = "EndDvceCtrlsCancelTopic";
    public static final String END_DEVICE_CONTROLS_CANCELLATION_TASK_SUBSCRIBER = "EndDvceCtrlsCancelSubscriber";
    public static final String END_DEVICE_CONTROLS_CANCELLATION_TASK_DISPLAYNAME = "End Device Controls cancellation";
    public static final int END_DEVICE_CONTROLS_CANCELLATION_TASK_RETRY_DELAY = 60;

    private volatile TaskService taskService;
    private volatile ServiceCallService serviceCallService;
    private volatile Clock clock;
    private volatile  MultiSenseHeadEndInterface multiSenseHeadEndInterface;

    public EndDeviceControlsCancellationHandlerFactory() {
        // for OSGI purpose
    }

    @Inject
    public EndDeviceControlsCancellationHandlerFactory(TaskService taskService, ServiceCallService serviceCallService,
                                                       Clock clock, MultiSenseHeadEndInterface multiSenseHeadEndInterface) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
        setClock(clock);
        setMultiSenseHeadEndInterface(multiSenseHeadEndInterface);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new EndDeviceControlsCancellationHandler(serviceCallService, clock, multiSenseHeadEndInterface));
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
    public final void setMultiSenseHeadEndInterface(MultiSenseHeadEndInterface multiSenseHeadEndInterface) {
        this.multiSenseHeadEndInterface = multiSenseHeadEndInterface;
    }
}
