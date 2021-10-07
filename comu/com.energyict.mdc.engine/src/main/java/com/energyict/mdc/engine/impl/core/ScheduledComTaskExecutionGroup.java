/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Models a group of ComTaskExecutions that need to
 * executed as a single job because the related
 * ConnectionTask
 * does not allow simultaneous connections.
 * Executing these ScheduledComTasks in parallel would cause
 * problems because each isolated execution would actually
 * require the ScheduledComTask to create a connection for each.
 */
public class ScheduledComTaskExecutionGroup extends ScheduledJobImpl {

    private ScheduledConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();

    public ScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.connectionTask = connectionTask;
    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        return this.connectionTask;
    }

    public void add(ComTaskExecution comTask) {
        this.comTaskExecutions.add(comTask);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        scheduledComTasks.addAll(this.comTaskExecutions);
        return scheduledComTasks;
    }

    @Override
    public boolean attemptLock() {
        return this.attemptLock(this.connectionTask);
    }

    @Override
    public void unlock() {
        this.unlock(this.connectionTask);
    }

    @Override
    public boolean isStillPending() {
        return this.getComServerDAO().areStillPending(this.collectIds(this.comTaskExecutions));
    }

    protected Collection<Long> collectIds(List<? extends HasId> hasIds) {
        Collection<Long> ids = new ArrayList<>(hasIds.size());
        for (HasId hasId : hasIds) {
            ids.add(hasId.getId());
        }
        return ids;
    }

    @Override
    public void execute() {
        try {
            boolean connectionEstablished = false;
            this.createExecutionContext();
            // TODO: measure duration of prepareAll
            commandRoot = this.prepareAll(this.comTaskExecutions);
            if (!commandRoot.hasGeneralSetupErrorOccurred() && !commandRoot.getCommands().isEmpty()) {
                connectionEstablished = this.establishConnectionFor(this.getComPort());
            }
            commandRoot.execute(connectionEstablished);
        } catch (Throwable e) {
            if (commandRoot == null) {
                commandRoot = initCommandRoot();    //Initialize it here so that the reschedule can happen.
            }
            commandRoot.generalSetupErrorOccurred(e, getComTaskExecutions());
            commandRoot.getExecutionContext().connectionLogger.taskExecutionFailed(e, Thread.currentThread().getName(), "General setup");
            throw e;
        } finally {
            try {
                this.completeConnection();
            } finally {
                this.closeConnection();
            }
        }
    }
}