package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetDeviceInActiveLifeCycleStatePostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.CreateDataLoggerCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateDataLoggerSlaveCommand;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
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

    private final DeviceService deviceService;
    private final ProtocolPluggableService protocolPluggableService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<DeviceBuilder> deviceBuilderProvider;
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;
    private final Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder;

    private String dataLoggerMrid;
    private String dataLoggerSerial;
    private Integer numberOfSlaves = 10;

    @Inject
    public  CreateDataLoggerSetupCommand(/* DeviceConfigurationService deviceConfigurationService ,*/
                                     DeviceService deviceService,
                                     ProtocolPluggableService protocolPluggableService,
                                     ConnectionTaskService connectionTaskService,
                                     Provider<DeviceBuilder> deviceBuilderProvider,
                                     Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider,
                                         Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder){
  //      this.deviceConfigurationService = deviceConfigurationService;
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

    public void setNumberOfSlaves(Integer numberOfSlaves){
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
        if (this.dataLoggerMrid != null)
            dataLoggerCommand.setDataLoggerMrid(dataLoggerMrid);
        if (this.dataLoggerSerial != null)
            dataLoggerCommand.setSerialNumber(dataLoggerMrid);

        dataLoggerCommand.run();
        createDataLoggerSlaves();
    }

    private void createDataLoggerSlaves(){
        for (int i=1; i<=numberOfSlaves; i++) {
            CreateDataLoggerSlaveCommand slave = new CreateDataLoggerSlaveCommand();
            slave.setMridPrefix(DeviceTypeTpl.EIMETER_FLEX.getLongName());
            slave.run();
        }
    }
}
