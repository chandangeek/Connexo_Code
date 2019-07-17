/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.Installer;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.PARENT_GET_METER_READINGS;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.SUBPARENT_GET_METER_READINGS;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.DEVICE_MESSAGE_GET_METER_READINGS;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_ID;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final BundleContext bundleContext;
    private final MessageService messageService;
    private final TaskService taskService;

    @Inject
    UpgraderV10_7(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                  BundleContext bundleContext, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.bundleContext = bundleContext;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        createFutureComTasksExecutionTask();
        removeOldGetMeterReadingsServiceCalls();
        createServiceCallTypes();
    }

    private void removeOldGetMeterReadingsServiceCalls() {
        serviceCallService.findServiceCallType(PARENT_GET_METER_READINGS.getTypeName(), PARENT_GET_METER_READINGS.getTypeVersion())
                .ifPresent(serviceCallType -> {
                    serviceCallType.getCustomPropertySets().stream()
                            .forEach(cps -> serviceCallType.removeCustomPropertySet(cps));

                    RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(CUSTOM_PROPERTY_SET_ID)
                            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set by id {0}", CUSTOM_PROPERTY_SET_ID)));
                    serviceCallType.addCustomPropertySet(registeredCustomPropertySet);
                    serviceCallType.save();
                });

        migrateSql();
    }

    private void migrateSql() {
        List<String> sql = new ArrayList<>();
        sql.add("DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension'");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private void createServiceCallTypes() {
        List<ServiceCallCommands.ServiceCallTypes> serviceCallTypes = new ArrayList<>();
        serviceCallTypes.add(PARENT_GET_METER_READINGS);
        serviceCallTypes.add(SUBPARENT_GET_METER_READINGS);
        serviceCallTypes.add(DEVICE_MESSAGE_GET_METER_READINGS);
        serviceCallTypes.add(COMTASK_EXECUTION_GET_METER_READINGS);
        for (ServiceCallCommands.ServiceCallTypes serviceCallType : serviceCallTypes) {
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

    private void createFutureComTasksExecutionTask() {
        String property = bundleContext.getProperty(Installer.RECURENT_TASK_FREQUENCY);
        createActionTask(FutureComTaskExecutionHandlerFactory.FUTURE_COM_TASK_EXECUTION_DESTINATION,
                Installer.RECURENT_TASK_RETRY_DELAY,
                TranslationKeys.FUTURE_COM_TASK_EXECUTION_NAME,
                Installer.RECURENT_TASK_NAME,
                property == null ? Installer.RECURENT_TASK_SCHEDULE : "0 0/" + property + " * 1/1 * ? *");
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
