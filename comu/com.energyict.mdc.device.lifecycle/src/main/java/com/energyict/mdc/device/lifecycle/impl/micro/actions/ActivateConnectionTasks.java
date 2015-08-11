package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will activate all connection tasks on the Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#ACTIVATE_CONNECTION_TASKS_IN_USE}
 * @since 2015-05-05 (12:49)
 */
public class ActivateConnectionTasks extends TranslatableServerMicroAction {

    public ActivateConnectionTasks(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getDeviceConfiguration().getComTaskEnablements()
                .stream().map(ComTaskEnablement::getPartialConnectionTask).flatMap(Functions.asStream()).distinct()
                .forEach(partialConnectionTask ->
                        device.getConnectionTasks().stream()
                                .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == partialConnectionTask.getId())
                                .findFirst().ifPresent(ConnectionTask::activate));
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE;
    }
}