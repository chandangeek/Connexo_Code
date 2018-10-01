/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

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
        device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !device.getComTaskExecutions().stream()
                        .map(comTaskExecution -> comTaskExecution.getComTask().getId())
                        .collect(Collectors.toList())
                        .contains(comTaskEnablement.getComTask().getId()))
                .map(comTaskEnablement -> createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement).add())
                .filter(not(ComTaskExecution::isOnHold))
                .forEach(ComTaskExecution::putOnHold);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.DISABLE_COMMUNICATION;
    }

    private ComTaskExecutionBuilder createManuallyScheduledComTaskExecutionWithoutFrequency(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
        }
        return manuallyScheduledComTaskExecutionComTaskExecutionBuilder;
    }
}