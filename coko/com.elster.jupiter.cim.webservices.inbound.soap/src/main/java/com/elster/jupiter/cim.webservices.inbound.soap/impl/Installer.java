/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.GetMeterReadingsCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.GetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
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
        createServiceCallType(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ParentGetMeterReadingsServiceCallHandler.VERSION,
                ParentGetMeterReadingsCustomPropertySet.class.getName());
        createServiceCallType(GetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetMeterReadingsServiceCallHandler.VERSION,
                GetMeterReadingsCustomPropertySet.class.getName());
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
}
