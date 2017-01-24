package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateG3GatewayCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateG3SlaveCommand;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Copyrights EnergyICT
 * Date: 23/09/2015
 * Time: 13:16
 */
public class CreateG3DemoBoardCommand {

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder;

    private String gatewayMrid = "Demo_board_RTU_Server_G3";

    @Inject
    public  CreateG3DemoBoardCommand(DeviceService deviceService,
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

    public void setGatewayMrid(String gatewayMrid) {
        this.gatewayMrid = gatewayMrid;
    }

    public void run() {
        CreateG3GatewayCommand gatewayCommand = new CreateG3GatewayCommand(
                deviceService,
                protocolPluggableService,
                connectionTaskService,
                deviceBuilderProvider,
                connectionMethodsProvider,
                activeLifeCyclestatePostBuilder);
        gatewayCommand.setGatewayName(gatewayMrid);
        gatewayCommand.setSerialNumber("Demo board RTU+Server G3");

        CreateG3SlaveCommand firstSlave = new CreateG3SlaveCommand(activeLifeCyclestatePostBuilder);
        firstSlave.setConfig("AS3000");

        CreateG3SlaveCommand secondSlave = new CreateG3SlaveCommand(activeLifeCyclestatePostBuilder);
        secondSlave.setConfig("AS220");

        gatewayCommand.run();
        firstSlave.run();
        secondSlave.run();

    }
}
