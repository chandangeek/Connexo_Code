/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckConfirmationTimeoutHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckScheduledRequestHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.ConnectionStatusChangeMessageHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.SearchDataSourceHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;

    // Properties
    private static final String CHECK_CONFIRMATION_TIMEOUT_FREQUENCY_PROPERTY = "com.elster.jupiter.sap.checkconfirmationtimeoutfrequency";
    private static final String REGISTER_SEARCH_INTERVAL_PROPERTY = "com.elster.jupiter.sap.registersearchinterval";
    private static final String CHECK_SCHEDULED_REQUEST_FREQUENCY_PROPERTY = "com.elster.jupiter.sap.checkscheduledrequestsfrequency";

    // Search data sources by SAP id's
    private static final String SEARCH_DATA_SOURCE_TASK_NAME = "SearchDataSourceTask";
    private static final String SEARCH_DATA_SOURCE_TASK_SCHEDULE = "0 0/5 * 1/1 * ? *";
    private static final int SEARCH_DATA_SOURCE_TASK_RETRY_DELAY = 60;

    // Check SAP confirmation timeout
    private static final String CHECK_CONFIRMATION_TIMEOUT_TASK_NAME = "CheckConfirmationTimeoutTask";
    private static final String CHECK_CONFIRMATION_TIMEOUT_TASK_SCHEDULE = "0 0/1 * 1/1 * ? *";
    private static final int CHECK_CONFIRMATION_TIMEOUT_TASK_RETRY_DELAY = 60;

    // Check scheduled SAP requests
    private static final String CHECK_SCHEDULED_REQUEST_TASK_NAME = "CheckScheduledRequestTask";
    private static final String CHECK_SCHEDULED_REQUEST_TASK_SCHEDULE = "0 0/60 * 1/1 * ? *";
    private static final int CHECK_SCHEDULED_REQUEST_TASK_RETRY_DELAY = 60;

    private final BundleContext bundleContext;
    private final CustomPropertySetService customPropertySetService;
    private final TaskService taskService;
    private final MessageService messageService;
    private final ServiceCallService serviceCallService;

    @Inject
    public Installer(BundleContext bundleContext, ServiceCallService serviceCallService,
                     CustomPropertySetService customPropertySetService, MessageService messageService,
                     TaskService taskService) {
        this.bundleContext = bundleContext;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Search data source task",
                this::createSearchDataSourceTask,
                logger
        );
        doTry(
                "Check confirmation timeout task",
                this::createCheckConfirmationTimeoutTask,
                logger
        );
        doTry(
                "Check scheduled request task",
                this::createCheckScheduledRequestTask,
                logger
        );
        doTry(
                "Create service call types",
                this::createServiceCallTypes,
                logger
        );
        doTry(
                "Create destination specs",
                this::createDestinationSpecs,
                logger
        );
    }

    private void createSearchDataSourceTask() {
        String property = bundleContext.getProperty(REGISTER_SEARCH_INTERVAL_PROPERTY);
        createActionTask(SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_DESTINATION,
                SEARCH_DATA_SOURCE_TASK_RETRY_DELAY,
                TranslationKeys.SEARCH_DATA_SOURCE_SUBSCRIBER_NAME,
                SEARCH_DATA_SOURCE_TASK_NAME,
                property == null ? SEARCH_DATA_SOURCE_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createCheckConfirmationTimeoutTask() {
        String property = bundleContext.getProperty(CHECK_CONFIRMATION_TIMEOUT_FREQUENCY_PROPERTY);
        createActionTask(CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_DESTINATION,
                CHECK_CONFIRMATION_TIMEOUT_TASK_RETRY_DELAY,
                TranslationKeys.CHECK_CONFIRMATION_TIMEOUT_SUBSCRIBER_NAME,
                CHECK_CONFIRMATION_TIMEOUT_TASK_NAME,
                property == null ? CHECK_CONFIRMATION_TIMEOUT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createCheckScheduledRequestTask() {
        String property = bundleContext.getProperty(CHECK_SCHEDULED_REQUEST_FREQUENCY_PROPERTY);
        createActionTask(CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_DESTINATION,
                CHECK_SCHEDULED_REQUEST_TASK_RETRY_DELAY,
                TranslationKeys.CHECK_SCHEDULED_REQUEST_SUBSCRIBER_NAME,
                CHECK_SCHEDULED_REQUEST_TASK_NAME,
                property == null ? CHECK_SCHEDULED_REQUEST_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
    }

    private void createActionTask(String destinationSpecName, int destinationSpecRetryDelay, TranslationKey subscriberSpecName, String taskName, String taskSchedule) {
        DestinationSpec destination = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE")
                .get()
                .createDestinationSpec(destinationSpecName, destinationSpecRetryDelay);
        destination.activate();
        destination.subscribe(subscriberSpecName, WebServiceActivator.COMPONENT_NAME, Layer.DOMAIN);

        taskService.newBuilder()
                .setApplication("Admin")
                .setName(taskName)
                .setScheduleExpressionString(taskSchedule)
                .setDestination(destination)
                .setPayLoad("payload")
                .scheduleImmediately(true)
                .build();
    }

    private void createServiceCallTypes() {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    private void createServiceCallType(ServiceCallTypes serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallType.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService
                    .findActiveCustomPropertySet(serviceCallTypeMapping.getPersistenceSupportClass())
                    .orElseThrow(() -> new IllegalStateException(
                            MessageFormat.format("Could not find active custom property set {0}",
                                    serviceCallTypeMapping.getCustomPropertySetClass())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion(), serviceCallTypeMapping.getApplication().orElse(null))
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

    private void createDestinationSpecs() {
        if (messageService.getDestinationSpec(ConnectionStatusChangeMessageHandlerFactory.DESTINATION).isPresent()) {
            throw new IllegalStateException(MessageFormat.format("Destination specification ''{0}'' already exists",
                    ConnectionStatusChangeMessageHandlerFactory.DESTINATION));
        }
        Optional<QueueTableSpec> queueTableSpec = messageService
                .getQueueTableSpec(ConnectionStatusChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME);
        if (queueTableSpec.isPresent()) {
            DestinationSpec destinationSpec = queueTableSpec.get()
                    .createDestinationSpec(ConnectionStatusChangeMessageHandlerFactory.DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationKeys.CONNECTION_STATUS_CHANGE_MESSAGE_HANDLER, WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
        } else {
            throw new IllegalStateException(MessageFormat.format("Queue table specification ''{0}'' is not available",
                    ConnectionStatusChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME));
        }
    }
}