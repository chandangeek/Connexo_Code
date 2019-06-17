/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {
    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;
    private static final String RECURENT_TASK_FREQUENCY = "com.energyict.mdc.cim.webservices.inbound.soap.recurenttaskfrequency";
    private static final String RECURENT_TASK_NAME = "CheckConfirmationTimeoutTask";
    private static final String RECURENT_TASK_SCHEDULE = "0 0/5 * 1/1 * ? *";
    private static final int RECURENT_TASK_RETRY_DELAY = 60;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;
    private final TaskService taskService;
    private final BundleContext bundleContext;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                     MessageService messageService, TaskService taskService, BundleContext bundleContext) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
        this.taskService = taskService;
        this.bundleContext = bundleContext;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create service call types",
                this::createServiceCallTypes,
                logger
        );
//        doTry(
//                "Create future com tasks execution task",
//                this::createFutureComTasksExecutionTask,
//                logger
//        );
//        doTry(
//                "Create destination specs",
//                this::createDestinationSpecs,
//                logger
//        );
    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypes serviceCallType : ServiceCallCommands.ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypes serviceCallType) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            try {
                CustomPropertySet customPropertySet = (CustomPropertySet) Class.forName(serviceCallType.getCustomPropertySetClass()).getConstructor().newInstance();

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId())
                        .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", customPropertySet.getClass().getSimpleName())));

                serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                        .handler(serviceCallType.getTypeName())
                        .logLevel(LogLevel.FINEST)
                        .customPropertySet(registeredCustomPropertySet)
                        .create();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

//    private void createDestinationSpecs() {
//        if (!messageService.getDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION).isPresent()) {
//            Optional<QueueTableSpec> queueTableSpec = messageService
//                    .getQueueTableSpec(ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME);
//            if (queueTableSpec.isPresent()) {
//                DestinationSpec destinationSpec = queueTableSpec.get()
//                        .createDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
//                destinationSpec.activate();
//                destinationSpec.subscribe(TranslationKeys.READ_METER_CHANGE_MESSAGE_HANDLER, InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
//            } else {
//                throw new IllegalStateException(MessageFormat.format("Queue table specification ''{0}'' is not available",
//                        ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME));
//            }
//        }
//
//    }

    private void createFutureComTasksExecutionTask() {
        String property = bundleContext.getProperty(RECURENT_TASK_FREQUENCY);
        createActionTask(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION,
                RECURENT_TASK_RETRY_DELAY,
                TranslationKeys.FUTURE_COM_TASK_EXECUTION_NAME,
                RECURENT_TASK_NAME,
                property == null ? RECURENT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                .get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("Admin")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }
}
