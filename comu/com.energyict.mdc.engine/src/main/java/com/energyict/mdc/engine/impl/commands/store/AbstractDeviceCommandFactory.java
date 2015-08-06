package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 05.08.15
 * Time: 16:15
 */
public abstract class AbstractDeviceCommandFactory implements DeviceCommandFactory {

    abstract MeterDataStoreCommandImpl getMeterDataStoreCommand(DeviceCommand.ServiceProvider serviceProvider);

    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, DeviceCommand.ServiceProvider serviceProvider) {
        List<DeviceCommand> deviceCommands = new ArrayList<>(collectedData.size());
        if (!collectedData.isEmpty()) {
            MeterDataStoreCommand meterDataStoreCommand = getMeterDataStoreCommand(serviceProvider);
            collectedData
                .stream()
                .map(serverCollectedData -> serverCollectedData.toDeviceCommand(meterDataStoreCommand, serviceProvider))
                .forEach(deviceCommands::add);
            deviceCommands.add(meterDataStoreCommand);
        }
        return deviceCommands;
    }
}
