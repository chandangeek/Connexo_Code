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
public class DeviceCommandFactoryImpl extends AbstractDeviceCommandFactory {

    @Override
    MeterDataStoreCommandImpl getMeterDataStoreCommand(DeviceCommand.ServiceProvider serviceProvider) {
        return new MeterDataStoreCommandImpl(serviceProvider);
    }
}