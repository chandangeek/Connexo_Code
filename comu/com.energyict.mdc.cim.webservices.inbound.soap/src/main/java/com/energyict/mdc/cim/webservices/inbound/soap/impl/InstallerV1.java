/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.eventhandler.ComTaskExecutionEventHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Optional;

public class InstallerV1 {
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;

    @Inject
    public InstallerV1(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, MessageService messageService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
    }

    public void createServiceCallTypes() {
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

                serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion(), serviceCallType.getApplication().orElse(null))
                        .handler(serviceCallType.getTypeName())
                        .logLevel(LogLevel.FINEST)
                        .customPropertySet(registeredCustomPropertySet)
                        .create();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    public void addJupiterEventSubscribers(){
        this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                .ifPresent(jupiterEvents -> {
                    boolean subscriberExists = jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(TranslationKeys.COM_TASK_EXECUTION_EVENT_SUBSCRIBER.getKey()));

                    if (!subscriberExists) {
                        jupiterEvents.subscribe(TranslationKeys.COM_TASK_EXECUTION_EVENT_SUBSCRIBER,
                                InboundSoapEndpointsActivator.COMPONENT_NAME,
                                Layer.SOAP,
                                DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.SCHEDULED_COMTASKEXECUTION_COMPLETED.topic())
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.SCHEDULED_COMTASKEXECUTION_FAILED.topic()))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_COMPLETED.topic()))
                                        .or(DestinationSpec.whereCorrelationId().isEqualTo(ComTaskExecutionEventHandler.EventType.MANUAL_COMTASKEXECUTION_FAILED.topic()))
                        );
                    }
                });
    }
}
