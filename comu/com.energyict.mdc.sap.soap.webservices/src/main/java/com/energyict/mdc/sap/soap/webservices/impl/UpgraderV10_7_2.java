/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7_2 implements Upgrader {
    private final DataModel dataModel;
    private final OrmService ormService;
    private final DeviceService deviceService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final UserService userService;
    private final SAPPrivilegeProvider sapPrivilegeProvider;
    private final Clock clock;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;


    @Inject
    public UpgraderV10_7_2(DataModel dataModel, OrmService ormService, DeviceService deviceService,
                           SAPCustomPropertySets sapCustomPropertySets, SAPPrivilegeProvider sapPrivilegeProvider, UserService userService,
                           Clock clock, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
        this.deviceService = deviceService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.sapPrivilegeProvider = sapPrivilegeProvider;
        this.userService = userService;
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7, 2));
        removeOldChannelAndRegisterSapCasValues();
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

    private void removeOldChannelAndRegisterSapCasValues() {
        ImmutableList.Builder<String> sqlQueries = ImmutableList.builder();
        addSqlQueriesForChannels(sqlQueries);
        addSqlQueriesForRegister(sqlQueries);

        try (Connection connection = this.dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            sqlQueries.build().forEach(oldColumn -> {
                try {
                    execute(statement, oldColumn);
                } catch (Exception e) {
                    // no action if column already not exists
                }
            });
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void addSqlQueriesForChannels(ImmutableList.Builder<String> sqlQueries) {
        Optional<DataModel> channelSapDataModel = ormService.getDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME);
        if (channelSapDataModel.isPresent()) {
            Set<Pair<Long, ChannelSpec>> cs = channelSapDataModel.get().stream(DeviceChannelSAPInfoDomainExtension.class)
                    .join(ChannelSpec.class)
                    .join(ReadingType.class)
                    .map(e -> Pair.of(e.getDeviceId(), e.getChannelSpec()))
                    .collect(Collectors.toSet());

            cs.forEach(csi -> {
                Optional<Device> device = deviceService.findDeviceById(csi.getFirst());
                if (device.isPresent()) {
                    if (device.get().getChannels().stream().map(c -> c.getChannelSpec()).noneMatch(f -> f.getId() == csi.getLast().getId())) {
                        sqlQueries.add("DELETE FROM SAP_CAS_DI2 WHERE DEVICE_ID = " + csi.getFirst() + " AND CHANNELSPEC = " + csi.getLast().getId());
                    }
                }
            });
        }
    }

    private void addSqlQueriesForRegister(ImmutableList.Builder<String> sqlQueries) {
        Optional<DataModel> registerSapDataModel = ormService.getDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME);
        if (registerSapDataModel.isPresent()) {
            Set<Pair<Long, RegisterSpec>> cs = registerSapDataModel.get().stream(DeviceRegisterSAPInfoDomainExtension.class)
                    .join(RegisterSpec.class)
                    .join(ReadingType.class)
                    .map(e -> Pair.of(e.getDeviceId(), e.getRegisterSpec()))
                    .collect(Collectors.toSet());

            cs.forEach(csi -> {
                Optional<Device> device = deviceService.findDeviceById(csi.getFirst());
                if (device.isPresent()) {
                    if (device.get().getRegisters().stream().map(c -> c.getRegisterSpec()).noneMatch(f -> f.getId() == csi.getLast().getId())) {
                        sqlQueries.add("DELETE FROM SAP_CAS_DI3 WHERE DEVICE = " + csi.getFirst() + " AND REGISTERSPEC = " + csi.getLast().getId());
                    }
                }
            });
        }
    }
}
