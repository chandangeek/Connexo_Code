package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;

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
    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, DeviceCommand.ServiceProvider serviceProvider) {
        List<DeviceCommand> deviceCommands = new ArrayList<>(collectedData.size());
        if (!collectedData.isEmpty()) {
            MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommand(serviceProvider);
            collectedData
                .stream()
                .map(serverCollectedData -> serverCollectedData.toDeviceCommand(meterDataStoreCommand, serviceProvider))
                .forEach(deviceCommands::add);
            deviceCommands.add(meterDataStoreCommand);
        }
        return deviceCommands;
    }

}