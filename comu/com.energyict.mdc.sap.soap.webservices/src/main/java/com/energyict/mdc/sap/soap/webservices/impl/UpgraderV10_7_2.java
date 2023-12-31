/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoDomainExtension;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7_2 implements Upgrader {
    private final DataModel dataModel;
    private final OrmService ormService;
    private final DeviceService deviceService;

    @Inject
    public UpgraderV10_7_2(DataModel dataModel, OrmService ormService, DeviceService deviceService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
        this.deviceService = deviceService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7, 2));
        removeOldChannelAndRegisterSapCasValues();
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
