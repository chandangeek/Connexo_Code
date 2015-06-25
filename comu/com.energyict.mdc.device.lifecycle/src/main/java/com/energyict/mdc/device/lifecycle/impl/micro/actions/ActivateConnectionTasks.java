package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will activate all connection tasks on the Device.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#ACTIVATE_CONNECTION_TASKS}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (12:49)
 */
public class ActivateConnectionTasks implements ServerMicroAction {

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ct -> ct.activate());
    }

}