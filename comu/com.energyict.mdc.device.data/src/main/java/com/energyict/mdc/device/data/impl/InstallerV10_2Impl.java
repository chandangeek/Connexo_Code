/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;
import com.energyict.mdc.device.data.security.Privileges;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter.MULTIPLIER_TYPE;

/**
 * Provides common functionality which can be used for clean install of 10.2 or for upgrade from 10.1 to 10.2
 */
public class InstallerV10_2Impl implements FullInstaller, PrivilegesProvider {

    private final UserService userService;
    private final MeteringService meteringService;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public InstallerV10_2Impl(UserService userService, MeteringService meteringService, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        super();
        this.userService = userService;
        this.meteringService = meteringService;
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
                "Create default multiplier type",
                this::createDefaultMultiplierType,
                logger
        );
    }

    @Override
    public String getModuleName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {

        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICES.getKey(), Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE, Privileges.Constants.VIEW_DEVICE, Privileges.Constants.REMOVE_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_DATA.getKey(), Privileges.RESOURCE_DEVICE_DATA_DESCRIPTION.getKey(), Arrays
                        .asList(Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_COMMUNICATIONS.getKey(), Privileges.RESOURCE_DEVICE_COMMUNICATIONS_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_GROUPS.getKey(), Privileges.RESOURCE_DEVICE_GROUPS_DESCRIPTION.getKey(), Arrays
                        .asList(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_INVENTORY_MANAGEMENT.getKey(), Privileges.RESOURCE_INVENTORY_MANAGEMENT_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT, Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DATA_COLLECTION_KPI.getKey(), Privileges.RESOURCE_DATA_COLLECTION_KPI_DESCRIPTION
                        .getKey(), Arrays.asList(Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI, Privileges.Constants.VIEW_DATA_COLLECTION_KPI)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICES
                        .getKey(), Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(), Collections
                        .singletonList(Privileges.Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS))
        );

    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
            createServiceCallType(serviceCallTypeMapping);
        }
        createOnDemandReadServiceCallType();
    }

    private void createOnDemandReadServiceCallType() {
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet()
                .getId())
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", OnDemandReadServiceCallCustomPropertySet.class
                        .getSimpleName())));
        RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CompletionOptionsCustomPropertySet()
                .getId())
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", CompletionOptionsCustomPropertySet.class
                        .getSimpleName())));


        serviceCallService.findServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                .orElseGet(() -> serviceCallService.createServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                        .handler(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                        .customPropertySet(customPropertySet)
                        .customPropertySet(completionOptionsCustomPropertySet)
                        .create());
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet commandCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CommandCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));
            RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CompletionOptionsCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(commandCustomPropertySet)
                    .customPropertySet(completionOptionsCustomPropertySet)
                    .create();
        }
    }

    private void createDefaultMultiplierType() {
        this.meteringService.createMultiplierType(MULTIPLIER_TYPE);
    }
}