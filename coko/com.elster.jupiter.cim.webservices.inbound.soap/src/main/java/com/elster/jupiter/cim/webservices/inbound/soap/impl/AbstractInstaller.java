/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.elster.jupiter.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;

public abstract class AbstractInstaller {

    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;

    final ServiceCallService serviceCallService;
    final CustomPropertySetService customPropertySetService;
    final MessageService messageService;

    public AbstractInstaller(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
            MessageService messageService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
    }

    void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypes serviceCallType : ServiceCallCommands.ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    void createServiceCallType(ServiceCallCommands.ServiceCallTypes serviceCallType) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService
                .findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            try {
                @SuppressWarnings("rawtypes")
                CustomPropertySet customPropertySet = (CustomPropertySet) Class
                        .forName(serviceCallType.getCustomPropertySetClass()).getConstructor().newInstance();

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService
                        .findActiveCustomPropertySet(customPropertySet.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                MessageFormat.format("Could not find active custom property set {0}",
                                        customPropertySet.getClass().getSimpleName())));

                serviceCallService
                        .createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion(), serviceCallType.getApplication().orElse(null))
                        .handler(serviceCallType.getTypeName()).logLevel(LogLevel.FINEST)
                        .customPropertySet(registeredCustomPropertySet).create();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
                    | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    void createDestinationSpecs() {
        if (!messageService.getDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION).isPresent()) {
            Optional<QueueTableSpec> queueTableSpec = messageService
                    .getQueueTableSpec(ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME);
            if (queueTableSpec.isPresent()) {
                DestinationSpec destinationSpec = queueTableSpec.get().createDestinationSpec(
                        ReadMeterChangeMessageHandlerFactory.DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
                destinationSpec.activate();
                destinationSpec.subscribe(TranslationKeys.READ_METER_CHANGE_MESSAGE_HANDLER,
                        CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
            } else {
                throw new IllegalStateException(
                        MessageFormat.format("Queue table specification ''{0}'' is not available",
                                ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME));
            }
        }

    }
}
