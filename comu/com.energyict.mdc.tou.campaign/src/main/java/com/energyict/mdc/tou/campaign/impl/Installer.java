/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final CustomPropertySetService customPropertySetService;
    private final ServiceCallService serviceCallService;

    @Inject
    public Installer(CustomPropertySetService customPropertySetService, ServiceCallService serviceCallService) {
        this.customPropertySetService = customPropertySetService;
        this.serviceCallService = serviceCallService;
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

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion(), createServiceCallLifeCycle(serviceCallTypeMapping))
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

    private ServiceCallLifeCycle createServiceCallLifeCycle(ServiceCallTypes serviceCallTypes) {
        switch (serviceCallTypes) {
            case TIME_OF_USE_CAMPAIGN:
                return serviceCallService
                        .createServiceCallLifeCycle("TimeOfUseCampaign")
                        .remove(DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.CREATED, DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.SCHEDULED, DefaultState.PENDING)
                        .removeTransition(DefaultState.SCHEDULED, DefaultState.CANCELLED)
                        .removeTransition(DefaultState.PARTIAL_SUCCESS, DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.FAILED, DefaultState.SCHEDULED)
                        .remove(DefaultState.PAUSED)
                        .removeTransition(DefaultState.ONGOING, DefaultState.PAUSED)
                        .removeTransition(DefaultState.PAUSED, DefaultState.CANCELLED)
                        .removeTransition(DefaultState.PAUSED, DefaultState.ONGOING)
                        .remove(DefaultState.WAITING)
                        .removeTransition(DefaultState.ONGOING, DefaultState.WAITING)
                        .removeTransition(DefaultState.WAITING, DefaultState.ONGOING)
                        .removeTransition(DefaultState.WAITING, DefaultState.CANCELLED)
                        .remove(DefaultState.PARTIAL_SUCCESS)
                        .removeTransition(DefaultState.ONGOING, DefaultState.PARTIAL_SUCCESS)
                        .removeTransition(DefaultState.PARTIAL_SUCCESS, DefaultState.SCHEDULED)
                        .remove(DefaultState.REJECTED)
                        .removeTransition(DefaultState.CREATED, DefaultState.REJECTED)
                        .addTransition(DefaultState.CREATED, DefaultState.ONGOING)
                        .create();
            case TIME_OF_USE_CAMPAIGN_ITEM:
            default:
                return serviceCallService
                        .createServiceCallLifeCycle("TimeOfUseItem")
                        .remove(DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.CREATED, DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.SCHEDULED, DefaultState.PENDING)
                        .removeTransition(DefaultState.SCHEDULED, DefaultState.CANCELLED)
                        .removeTransition(DefaultState.PARTIAL_SUCCESS, DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.FAILED, DefaultState.SCHEDULED)
                        .remove(DefaultState.PAUSED)
                        .removeTransition(DefaultState.ONGOING, DefaultState.PAUSED)
                        .removeTransition(DefaultState.PAUSED, DefaultState.CANCELLED)
                        .removeTransition(DefaultState.PAUSED, DefaultState.ONGOING)
                        .remove(DefaultState.WAITING)
                        .removeTransition(DefaultState.ONGOING, DefaultState.WAITING)
                        .removeTransition(DefaultState.WAITING, DefaultState.ONGOING)
                        .removeTransition(DefaultState.WAITING, DefaultState.CANCELLED)
                        .remove(DefaultState.PARTIAL_SUCCESS)
                        .removeTransition(DefaultState.ONGOING, DefaultState.PARTIAL_SUCCESS)
                        .removeTransition(DefaultState.PARTIAL_SUCCESS, DefaultState.SCHEDULED)
                        .removeTransition(DefaultState.CREATED, DefaultState.CANCELLED)
                        .removeTransition(DefaultState.CREATED, DefaultState.ONGOING)
                        .addTransition(DefaultState.REJECTED, DefaultState.PENDING)
                        .addTransition(DefaultState.REJECTED, DefaultState.FAILED)
                        .addTransition(DefaultState.FAILED, DefaultState.PENDING)
                        .addTransition(DefaultState.CANCELLED, DefaultState.PENDING)
                        .create();
        }
    }
}