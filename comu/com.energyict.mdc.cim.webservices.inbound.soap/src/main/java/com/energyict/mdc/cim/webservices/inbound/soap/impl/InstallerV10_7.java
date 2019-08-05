/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

public class InstallerV10_7 {
    public static final String RECURRENT_TASK_FREQUENCY = "com.energyict.mdc.cim.webservices.inbound.soap.recurrenttaskfrequency";
    public static final String RECURRENT_TASK_NAME = "Future com tasks execution task";
    public static final String RECURRENT_TASK_SCHEDULE = "0 0/5 * 1/1 * ? *";
    public static final int RECURRENT_TASK_RETRY_DELAY = 60;
    private final MessageService messageService;
    private final TaskService taskService;
    private final BundleContext bundleContext;

    @Inject
    public InstallerV10_7(MessageService messageService, TaskService taskService, BundleContext bundleContext) {
        this.messageService = messageService;
        this.taskService = taskService;
        this.bundleContext = bundleContext;
    }

    public void createFutureComTasksExecutionTask() {
        String property = bundleContext.getProperty(RECURRENT_TASK_FREQUENCY);
        createActionTask(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION,
                RECURRENT_TASK_RETRY_DELAY,
                TranslationKeys.FUTURE_COM_TASK_EXECUTION_NAME,
                RECURRENT_TASK_NAME,
                property == null ? RECURRENT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                .get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("MultiSense")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }
}
