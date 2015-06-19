package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable communication with the device
 * by putting all connection and communication tasks on hold.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#DISABLE_COMMUNICATION}
 *
 * action bits: 32
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (16:58)
 */
public class DisableCommunication implements ServerMicroAction {

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(connectionTask -> connectionTask.deactivate());
        device.getComTaskExecutions().forEach(ComTaskExecution::putOnHold);
    }

}