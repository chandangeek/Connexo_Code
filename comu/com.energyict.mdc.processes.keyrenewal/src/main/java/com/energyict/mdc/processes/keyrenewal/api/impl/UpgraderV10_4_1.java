/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRHandlerFactory;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.KeyRenewalCustomPropertySet;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.OperationHandler;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;

public class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    private static final int DESTINATION_SPEC_RETRY_DELAY = 60;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, MessageService messageService, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        createServiceCallTypes();
        createDestinationSpecs();
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

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(OperationHandler.HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

    private void createDestinationSpecs() {
        Optional<DestinationSpec> destinationSpecOptional  = messageService.getDestinationSpec(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec destinationSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get()
                    .createDestinationSpec(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_DESTINATION, DESTINATION_SPEC_RETRY_DELAY);
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationSeeds.CERTIFICATE_REQUEST_FOR_CSR_MESSAGE_HANDLER, KeyRenewalApplication.COMPONENT_NAME, Layer.REST);
        }
    }
}
