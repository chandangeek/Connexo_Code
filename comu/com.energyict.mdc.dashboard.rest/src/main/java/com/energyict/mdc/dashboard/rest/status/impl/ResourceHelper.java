/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;

import javax.inject.Inject;
import java.util.Optional;

public class ResourceHelper {
    private final CommunicationTaskService communicationTaskService;
    private final ConnectionTaskService connectionTaskService;
    private final PriorityComTaskService priorityComTaskService;

    @Inject
    public ResourceHelper(CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, PriorityComTaskService priorityComTaskService) {
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskService = connectionTaskService;
        this.priorityComTaskService = priorityComTaskService;
    }

    public Long getCurrentComTaskExecutionVersion(long id) {
        return communicationTaskService.findComTaskExecution(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComTaskExecution::getVersion)
                .orElse(null);
    }

    public Optional<ComTaskExecution> getLockedComTaskExecution(long id, long version) {
        return communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public Long getCurrentConnectionTaskVersion(long id) {
        return connectionTaskService.findConnectionTask(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ConnectionTask::getVersion)
                .orElse(null);
    }

    public Optional<ConnectionTask> getLockedConnectionTask(long id, long version) {
        return connectionTaskService.findAndLockConnectionTaskByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public Optional<ConnectionTask> getLockedConnectionTask(long id) {
        return connectionTaskService.findAndLockConnectionTaskById(id)
                .filter(candidate -> !candidate.isObsolete());
    }

    PriorityComTaskExecutionLink getLockedPriorityComTaskExecution(ComTaskExecution comTaskExecution) {
        Optional<PriorityComTaskExecutionLink> priorityComTaskExecutionLink = priorityComTaskService.findByComTaskExecution(comTaskExecution);
        return priorityComTaskExecutionLink.orElseGet(() -> priorityComTaskService.from(comTaskExecution));
    }
}