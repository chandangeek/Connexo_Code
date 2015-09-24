package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
    private ConnectionTypePluggableClass requiredPluggableClass;
    private final Provider<DeviceBuilder> deviceBuilderProvider;

    private String gatewayMrid = "Demo board RTU+Server G3";

    public  CreateG3DemoBoardCommand(DeviceService deviceService, ProtocolPluggableService protocolPluggableService,
                                      ConnectionTaskService connectionTaskService, Provider<DeviceBuilder> deviceBuilderProvider){
        this.deviceService = deviceService;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionTaskService = connectionTaskService;
        this.deviceBuilderProvider = deviceBuilderProvider;
    };

    public void setGatewayMrid(String gatewayMrid) {
        this.gatewayMrid = gatewayMrid;
    }

    public void run() {
        CreateG3GatewayCommand gatewayCommand = new CreateG3GatewayCommand(deviceService, protocolPluggableService, connectionTaskService,deviceBuilderProvider);
        gatewayCommand.setGatewayMrid(gatewayMrid);

        CreateG3SlaveCommand firstSlave = new CreateG3SlaveCommand();
        firstSlave.setConfig("AS3000");

        CreateG3SlaveCommand secondSlave = new CreateG3SlaveCommand();
        secondSlave.setConfig("AS220");

        gatewayCommand.run();
        firstSlave.run();
        secondSlave.run();

    }
}
