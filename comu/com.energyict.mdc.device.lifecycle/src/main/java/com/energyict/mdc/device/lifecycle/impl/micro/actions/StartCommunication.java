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
 * that will start the  communication with the device
 * by activating all connection schedule all communication tasks
 * to execute now.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#START_COMMUNICATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (14:32)
 */
public class StartCommunication implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(connectionTask -> connectionTask.activate());
        device.getComTaskExecutions().forEach(ComTaskExecution::scheduleNow);
    }

}