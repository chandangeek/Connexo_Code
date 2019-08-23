/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.processes.keyrenewal.api.impl.CompletionOptionsMessageHandlerFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.KeyRenewalApplication;
import com.energyict.mdc.processes.keyrenewal.api.impl.TranslationSeeds;
import com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRHandlerFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.KeyRenewalCustomPropertySet;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.OperationHandler;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;

    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public Installer(MessageService messageService, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.messageService = messageService;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create service call types",
                this::createServiceCallTypes,
                logger
        );
        doTry(
                "Create destination spec",
                this::createDestinationSpecs,
                logger
        );
    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypes serviceCallType : com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.ServiceCallCommands.ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypes serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new KeyRenewalCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", KeyRenewalCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion(), serviceCallTypeMapping.getApplication())
                    .handler(OperationHandler.HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

    private void createDestinationSpecs() {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec destinationSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                    .createDestinationSpec(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationSeeds.COMPLETION_OPTIONS_MESSAGE_HANDLER, KeyRenewalApplication.COMPONENT_NAME, Layer.REST);
        }
        destinationSpecOptional = messageService.getDestinationSpec(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec destinationSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                    .createDestinationSpec(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationSeeds.CERTIFICATE_REQUEST_FOR_CSR_MESSAGE_HANDLER, KeyRenewalApplication.COMPONENT_NAME, Layer.REST);
        }
    }

}