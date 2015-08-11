package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable communication with the device
 * by putting all connection and communication tasks on hold.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#DISABLE_COMMUNICATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (16:58)
 */
public class DisableCommunication extends TranslatableServerMicroAction {

    public DisableCommunication(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ConnectionTask::deactivate);
        device.getComTaskExecutions().forEach(ComTaskExecution::putOnHold);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.DISABLE_COMMUNICATION;
    }
}