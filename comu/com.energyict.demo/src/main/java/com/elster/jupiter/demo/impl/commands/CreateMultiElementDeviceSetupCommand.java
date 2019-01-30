/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateMultiElementDeviceCommand;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.inject.Provider;

public class CreateMultiElementDeviceSetupCommand extends CommandWithTransaction {

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder;

    private String name;
    private String serial;

    @Inject
    public CreateMultiElementDeviceSetupCommand(DeviceService deviceService,
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

    public void setName(String name) {
        this.name = name;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void run() {
        CreateMultiElementDeviceCommand multiElementDeviceCommand = new CreateMultiElementDeviceCommand(
                deviceService,
                protocolPluggableService,
                connectionTaskService,
                deviceBuilderProvider,
                connectionMethodsProvider,
                activeLifeCyclestatePostBuilder
        );
        if (this.name != null) {
            multiElementDeviceCommand.setMultiElementDeviceName(name);
        }
        if (this.serial != null) {
            multiElementDeviceCommand.setSerialNumber(serial);
        }

        multiElementDeviceCommand.run();
    }
}
