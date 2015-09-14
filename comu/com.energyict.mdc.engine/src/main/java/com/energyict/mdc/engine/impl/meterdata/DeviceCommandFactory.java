package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;

import java.util.List;

/**
 * Provides factory services for DeviceCommands
 * that are created from {@link com.energyict.mdc.protocol.api.device.data.CollectedData}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:35)
 */
public interface DeviceCommandFactory {

    /**
     * Creates a {@link DeviceCommand} for every {@link ServerCollectedData}.
     *
     * @param collectedData The List of ServerCollectedData
     * @param serviceProvider The {@link DeviceCommand.ServiceProvider}
     * @return The List of DeviceCommands
     */
    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, DeviceCommand.ServiceProvider serviceProvider);

}