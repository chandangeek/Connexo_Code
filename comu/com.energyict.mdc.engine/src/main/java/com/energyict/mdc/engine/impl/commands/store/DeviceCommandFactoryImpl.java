/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.meterdata.DeviceCommandFactory;

/**
 * Provides an implementation for the {@link DeviceCommandFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:51)
 */
public class DeviceCommandFactoryImpl extends AbstractDeviceCommandFactory {

    private final ComTaskExecution comTaskExecution;

    public DeviceCommandFactoryImpl(ComTaskExecution comTaskExecution) {
        super();
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    MeterDataStoreCommandImpl getMeterDataStoreCommand(DeviceCommand.ServiceProvider serviceProvider) {
        return new MeterDataStoreCommandImpl(this.comTaskExecution, serviceProvider);
    }

}