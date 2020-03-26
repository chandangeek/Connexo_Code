/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will activate all recurring communication with the device.
 *
 * @see {@link MicroAction#ACTIVATE_ALL_RECURRING_COMMUNICATIONS}
 * @since 2020-03-26 (16:58)
 */
public class ActivateAllRecurringCommunications extends TranslatableServerMicroAction {

    protected final DeviceService deviceService;

    public ActivateAllRecurringCommunications(Thesaurus thesaurus, DeviceService deviceService) {
        super(thesaurus);
        this.deviceService = deviceService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ConnectionTask::activate);
        device.getComTaskExecutions().stream()
                .forEach(comTaskExecution -> {
                    if (comTaskExecution.isOnHold() && comTaskExecution.getPlannedNextExecutionTimestamp() != null) {
                        comTaskExecution.resume();
                    }
                });

        deviceService.findDeviceById(device.getId())
                .ifPresent(modDevice -> modDevice.getDeviceConfiguration().getComTaskEnablements().stream()
                        .filter(comTaskEnablement -> !modDevice.getComTaskExecutions().stream()
                                .map(comTaskExecution -> comTaskExecution.getComTask().getId())
                                .collect(Collectors.toList())
                                .contains(comTaskEnablement.getComTask().getId()))
                        .map(comTaskEnablement -> createManuallyScheduledComTaskExecutionWithoutFrequency(modDevice, comTaskEnablement).add())
                        .forEach(comTaskExecution -> {
                                    if (comTaskExecution.isOnHold() && comTaskExecution.getPlannedNextExecutionTimestamp() != null) {
                                        comTaskExecution.resume();
                                    }
                                }
                        ));
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.ACTIVATE_ALL_RECURRING_COMMUNICATIONS;
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


