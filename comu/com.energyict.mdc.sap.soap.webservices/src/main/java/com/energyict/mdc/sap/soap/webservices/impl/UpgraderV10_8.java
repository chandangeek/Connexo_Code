/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_8 implements Upgrader {
    private final DataModel dataModel;
    private final OrmService ormService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final UserService userService;
    private final SAPPrivilegeProvider sapPrivilegeProvider;
    private final Clock clock;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;


    @Inject
    public UpgraderV10_8(DataModel dataModel, OrmService ormService,
                         SAPCustomPropertySets sapCustomPropertySets, SAPPrivilegeProvider sapPrivilegeProvider, UserService userService,
                         Clock clock, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.sapPrivilegeProvider = sapPrivilegeProvider;
        this.userService = userService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 8));
        updateRegisteredFlag();
        userService.addModulePrivileges(sapPrivilegeProvider);
        createNewlyAddedServiceCallTypes();
    }

    private void createNewlyAddedServiceCallTypes() {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            createNewlyAddedServiceCallType(serviceCallType);
        }
    }

    private void createNewlyAddedServiceCallType(ServiceCallTypes serviceCallTypeMapping) {
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

    private void updateRegisteredFlag() {
        ImmutableList.Builder<String> sqlQueries = ImmutableList.builder();
        Optional<DataModel> deviceSapDataModel = ormService.getDataModel(DeviceSAPInfoCustomPropertySet.MODEL_NAME);
        if (deviceSapDataModel.isPresent()) {
            deviceSapDataModel.get().stream(DeviceSAPInfoDomainExtension.class)
                    .join(Device.class)
                    .filter(Where.where(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.javaName()).isEqualTo(false))
                    .map(DeviceSAPInfoDomainExtension::getDevice)
                    .forEach(device -> {
                        if (device.getStage().getName().equals(EndDeviceStage.OPERATIONAL.getKey()) && sapCustomPropertySets.isAnyLrnPresent(device.getId(), clock.instant())) {
                            sqlQueries.add("UPDATE SAP_CAS_DI1 SET REGISTERED = 'Y' WHERE DEVICE = " + device.getId());
                        }
                    });

            try (Connection connection = this.dataModel.getConnection(true);
                 Statement statement = connection.createStatement()) {
                sqlQueries.build().forEach(oldColumn -> {
                    execute(statement, oldColumn);
                });
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
