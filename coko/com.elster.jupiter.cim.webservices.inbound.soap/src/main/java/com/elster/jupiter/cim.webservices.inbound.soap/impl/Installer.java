/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.task.ReadMeterChangeMessageHandlerFactory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {
    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, MessageService messageService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
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

    private void createServiceCallTypes() {
        createServiceCallType(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ParentGetMeterReadingsServiceCallHandler.VERSION,
                ParentGetMeterReadingsCustomPropertySet.class.getName());
    }

    private void createServiceCallType(String handlerName, String version, String propertySetName) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(handlerName, version);
        if (!serviceCallTypeOptional.isPresent()) {
            try {
                CustomPropertySet customPropertySet = (CustomPropertySet) Class.forName(propertySetName).getConstructor().newInstance();

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId())
                        .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", customPropertySet.getClass().getSimpleName())));

                serviceCallService.createServiceCallType(handlerName, version)
                        .handler(handlerName)
                        .logLevel(LogLevel.FINEST)
                        .customPropertySet(registeredCustomPropertySet)
                        .create();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private void createDestinationSpecs() {
        if (messageService.getDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION).isPresent()) {
            return;
        }
        Optional<QueueTableSpec> queueTableSpec = messageService
                .getQueueTableSpec(ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME);
        if (queueTableSpec.isPresent()) {
            DestinationSpec destinationSpec = queueTableSpec.get()
                    .createDestinationSpec(ReadMeterChangeMessageHandlerFactory.DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationKeys.READ_METER_CHANGE_MESSAGE_HANDLER, CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
        } else {
            throw new IllegalStateException(MessageFormat.format("Queue table specification ''{0}'' is not available",
                    ReadMeterChangeMessageHandlerFactory.QUEUE_TABLE_SPEC_NAME));
        }
    }
}
