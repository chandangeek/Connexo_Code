package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetDeviceInActiveLifeCycleStatePostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateDataLoggerCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateG3SlaveCommand;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Setup for a 'realistic' Data Logger/ Data Logger Slave
 * Copyrights EnergyICT
 * Date: 20/06/2016
 * Time: 9:13
 */
public class CreateDataLoggerSetupCommand {
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder;

    private String dataLoggerMrid = "DataLogger32" ;

    @Inject
    public  CreateDataLoggerSetupCommand(DeviceConfigurationService deviceConfigurationService,
                                     DeviceService deviceService,
                                     ProtocolPluggableService protocolPluggableService,
                                     ConnectionTaskService connectionTaskService,
                                     Provider<DeviceBuilder> deviceBuilderProvider,
                                     Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                         Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder){
        this.deviceConfigurationService = deviceConfigurationService;
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

    public void run() {
        CreateDataLoggerCommand dataLoggerCommand = new CreateDataLoggerCommand(
                deviceService,
                protocolPluggableService,
                connectionTaskService,
                deviceBuilderProvider,
                connectionMethodsProvider,
                activeLifeCyclestatePostBuilder
        );
        dataLoggerCommand.setDataLoggerMrid(dataLoggerMrid);
        dataLoggerCommand.setSerialNumber("DataLogger32");

//        CreateG3SlaveCommand firstSlave = new CreateG3SlaveCommand(activeLifeCyclestatePostBuilder);
//        firstSlave.setConfig("AS3000");
//
//        CreateG3SlaveCommand secondSlave = new CreateG3SlaveCommand(activeLifeCyclestatePostBuilder);
//        secondSlave.setConfig("AS220");

        dataLoggerCommand.run();
//        firstSlave.run();
//        secondSlave.run();

    }
}
