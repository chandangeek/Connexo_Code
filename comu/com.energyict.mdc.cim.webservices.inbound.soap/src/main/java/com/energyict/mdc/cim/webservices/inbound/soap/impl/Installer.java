/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
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
    }

    private void createServiceCallTypes() {
        createMasterServiceCallType();
        createServiceCallType();
    }

    private void createMasterServiceCallType() {
        ServiceCallCommands.ServiceCallTypes serviceCallType = ServiceCallCommands.ServiceCallTypes.MASTER_METER_CONFIG;
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallType.getTypeName(),
                serviceCallType.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new MeterConfigMasterCustomPropertySet()
                    .getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}",
                            MeterConfigMasterCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .handler(MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

    private void createServiceCallType() {
        ServiceCallCommands.ServiceCallTypes serviceCallType = ServiceCallCommands.ServiceCallTypes.METER_CONFIG;
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallType.getTypeName(),
                serviceCallType.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new MeterConfigCustomPropertySet()
                    .getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}",
                            MeterConfigCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .handler(MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }
}
