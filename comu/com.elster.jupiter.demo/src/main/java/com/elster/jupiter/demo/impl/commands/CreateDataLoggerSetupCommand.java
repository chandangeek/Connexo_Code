/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.FavoriteGroupBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateDataLoggerCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateDataLoggerSlaveCommand;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceGroupTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class CreateDataLoggerSetupCommand extends CommandWithTransaction {

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder;

    private String dataLoggerMrid;
    private String dataLoggerSerial;
    private Integer numberOfSlaves = 10;

    @Inject
    public CreateDataLoggerSetupCommand(DeviceService deviceService,
                                        ProtocolPluggableService protocolPluggableService,
                                        ConnectionTaskService connectionTaskService,
                                        Provider<DeviceBuilder> deviceBuilderProvider,
                                        Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                        Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder) {

        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
        this.connectionMethodsProvider = connectionMethodsProvider;
        this.activeLifeCyclestatePostBuilder = activeLifeCyclestatePostBuilder;
    }

    public void setDataLoggerMrid(String mRid) {
        this.dataLoggerMrid = mRid;
    }

    public void setDataLoggerSerial(String serial) {
        this.dataLoggerSerial = serial;
    }

    public void setNumberOfSlaves(Integer numberOfSlaves) {
        this.numberOfSlaves = numberOfSlaves;
    }

    public void run() {
        CreateDataLoggerCommand dataLoggerCommand = new CreateDataLoggerCommand(
                deviceService,
                protocolPluggableService,
                connectionTaskService,
                deviceBuilderProvider,
                connectionMethodsProvider,
                activeLifeCyclestatePostBuilder
        );
        if (this.dataLoggerMrid != null) {
            dataLoggerCommand.setDataLoggerName(dataLoggerMrid);
        }
        if (this.dataLoggerSerial != null) {
            dataLoggerCommand.setSerialNumber(dataLoggerSerial);
        }

        dataLoggerCommand.run();
        if (numberOfSlaves > 0) {
            createDataLoggerSlaves();
        }
    }

    private void createDataLoggerSlaves() {
        //1. Create Device Type
        DeviceType deviceType = Builders.from(DeviceTypeTpl.EIMETER_FLEX).get();
        //1. Create Device Configuration and activate
        Optional<DeviceConfiguration> existingConfiguration = deviceType.getConfigurations()
                .stream()
                .filter(each -> DeviceConfigurationTpl.DATA_LOGGER_SLAVE.getName().equals(each.getName()))
                .findFirst();
        DeviceConfiguration deviceConfiguration;
        if (existingConfiguration.isPresent()) {
            deviceConfiguration = existingConfiguration.get();
        } else {
            deviceConfiguration = Builders.from(DeviceConfigurationTpl.DATA_LOGGER_SLAVE)
                    .withDeviceType(deviceType)
                    .withDirectlyAddressable(false)
                    .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                    .create();
        }
        if (!deviceConfiguration.isActive()) {
            deviceConfiguration.activate();
        }
        //3. Create Device Group "Data logger slaves"
        EndDeviceGroup dataLoggerSlaveGroup = Builders.from(DeviceGroupTpl.DATA_LOGGER_SLAVES).get();
        Builders.from(FavoriteGroupBuilder.class).withGroup(dataLoggerSlaveGroup).get();

        //4. Create "Data logger slave" devices
        int existing = deviceService.findDevicesByDeviceConfiguration(deviceConfiguration).find().size();
        for (int i = existing + 1; i <= existing + numberOfSlaves; i++) {
            CreateDataLoggerSlaveCommand slave = new CreateDataLoggerSlaveCommand();
            slave.setActiveLifeCyclestatePostBuilder(this.activeLifeCyclestatePostBuilder);
            slave.setDeviceNamePrefix(DeviceTypeTpl.EIMETER_FLEX.getName());
            slave.setSerialNumber("" + i);
            slave.run();
        }
    }
}
