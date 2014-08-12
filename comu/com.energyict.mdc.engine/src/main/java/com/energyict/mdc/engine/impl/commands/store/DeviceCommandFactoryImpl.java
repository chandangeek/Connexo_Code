package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceCommandFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:51)
 */
public class DeviceCommandFactoryImpl implements DeviceCommandFactory {

    @Override
    public CompositeDeviceCommand newCompositeForAll(List<ServerCollectedData> collectedData, ComServer.LogLevel communicationLogLevel, IssueService issueService, ComTaskExecutionSessionBuilder builder) {
        CompositeDeviceCommand composite = new ComSessionRootDeviceCommand(communicationLogLevel);
        for (DeviceCommand command : this.newForAll(collectedData, issueService)) {
            composite.add(command);
        }
        return composite;
    }

    @Override
    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, IssueService issueService) {
        List<DeviceCommand> deviceCommands = new ArrayList<>(collectedData.size());
        if(collectedData.size() > 0){
            MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommand();
            for (ServerCollectedData serverCollectedData : collectedData) {
                deviceCommands.add(serverCollectedData.toDeviceCommand(issueService, meterDataStoreCommand));
            }
            deviceCommands.add(meterDataStoreCommand);
        }
        return deviceCommands;
    }

}