/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable communication with the device
 * by putting all connection and communication tasks on hold.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#DISABLE_COMMUNICATION}
 * @since 2015-05-05 (16:58)
 */
public class DisableCommunication extends TranslatableServerMicroAction {
    protected final DeviceService deviceService;
    private final CommunicationTaskService communicationTaskService;
    private final ConnectionTaskService connectionTaskService;

    public DisableCommunication(Thesaurus thesaurus, DeviceService deviceService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService) {
        super(thesaurus);
        this.deviceService = deviceService;
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().stream()
                .map(ConnectionTask::getId)
                .sorted()
                .map(connectionTaskService::findAndLockConnectionTaskById)
                .flatMap(Functions.asStream())
                .forEach(ConnectionTask::deactivate);
        device.getComTaskExecutions().stream()
                .map(ComTaskExecution::getId)
                .sorted()
                .map(communicationTaskService::findAndLockComTaskExecutionById)
                .flatMap(Functions.asStream())
                .forEach(ComTaskExecution::putOnHold);
        deviceService.findDeviceById(device.getId())
                .ifPresent(modDevice -> {
                    Set<Long> comTasksWithExecutions = modDevice.getComTaskExecutions().stream()
                            .map(comTaskExecution -> comTaskExecution.getComTask().getId())
                            .collect(Collectors.toSet());
                    modDevice.getDeviceConfiguration().getComTaskEnablements().stream()
                            .filter(comTaskEnablement -> !comTasksWithExecutions.contains(comTaskEnablement.getComTask().getId()))
                            .map(comTaskEnablement -> createManuallyScheduledComTaskExecutionWithoutFrequency(modDevice, comTaskEnablement).add())
                            .filter(not(ComTaskExecution::isOnHold))
                            .forEach(ComTaskExecution::putOnHold);
                });
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
