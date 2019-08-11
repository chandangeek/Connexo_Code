/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HighPriorityParallelRootScheduledJob extends ParallelRootScheduledJob {

    private List<PriorityComTaskExecutionLink> comTaskExecutions = new ArrayList<>();

    public HighPriorityParallelRootScheduledJob(OutboundComPort comPort, ComServerDAO comServerDAO,
                                                DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask,
                                                CountDownLatch start,
                                                ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, start, serviceProvider);
    }

    public void add(PriorityComTaskExecutionLink priorityComTaskExecutionLink) {
        comTaskExecutions.add(priorityComTaskExecutionLink);
        super.add(priorityComTaskExecutionLink.getComTaskExecution());
    }

    @Override
    public boolean isStillPending() {
        return getComServerDAO().areStillPendingWithHighPriority(collectIds(comTaskExecutions));
    }

    @Override
    protected RescheduleBehavior getRescheduleBehavior() {
        return new RescheduleBehaviorForAsap(getComServerDAO(), super.getConnectionTask(), getServiceProvider().clock());
    }

    @Override
    public boolean isHighPriorityJob() {
        return true;
    }

    @Override
    public boolean attemptLock() {
        boolean lockState = true;
        // Lock the individual HighPriorityComTaskExecutions
        for (PriorityComTaskExecutionLink comTaskExecution : comTaskExecutions) {
            lockState = lockState && attemptLock(comTaskExecution);
        }
        super.attemptLock(); // which will lock the ConnectionTask (if not already locked)
        return lockState;
    }

    @Override
    protected void unlock(ScheduledConnectionTask connectionTask) {
        // no need to unlock the individual HighPriorityComTaskExecutions - they are anyway deleted on task end
        super.unlock(connectionTask);
    }
}
