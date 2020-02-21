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
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.task.ConnectionStatusChangeMessageHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.security.Privileges;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {

    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;

    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final UserService userService;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                     MessageService messageService, UserService userService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
        this.userService = userService;
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
        userService.addModulePrivileges(this);
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

    @Override
    public String getModuleName() {
        return WebServiceActivator.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SAP.getKey(), Privileges.RESOURCE_SAP_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.SEND_WEB_SERVICE_REQUEST)));
        return resources;
    }
}