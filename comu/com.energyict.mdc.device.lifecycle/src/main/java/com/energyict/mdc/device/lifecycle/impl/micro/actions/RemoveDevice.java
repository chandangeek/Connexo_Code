package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will delete the given Device
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#REMOVE_DEVICE}
 *
 * Copyrights EnergyICT
 * Date: 29/06/15
 * Time: 10:17
 */
public class RemoveDevice implements ServerMicroAction {

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.delete();
    }
}
