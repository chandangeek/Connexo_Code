/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class StartRecurringCommunication extends TranslatableServerMicroAction {

    private CommunicationTaskService communicationTaskService;
    private ConnectionTaskService connectionTaskService;

    public StartRecurringCommunication(Thesaurus thesaurus, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService) {
        super(thesaurus);
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().stream()
                .map(connectionTask -> connectionTaskService.findAndLockConnectionTaskById(connectionTask.getId()))
                .map(Optional::get)
                .forEach(ConnectionTask::activate);
        device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getPlannedNextExecutionTimestamp() != null)
                .map(comTaskExecution -> communicationTaskService.findAndLockComTaskExecutionById(comTaskExecution.getId()))
                .map(Optional::get)
                .forEach(ComTaskExecution::scheduleNow);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.START_RECURRING_COMMUNICATION;
    }
}