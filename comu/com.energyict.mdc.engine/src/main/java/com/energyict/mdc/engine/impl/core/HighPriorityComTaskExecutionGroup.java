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

public class HighPriorityComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

    private List<PriorityComTaskExecutionLink> priorityComTaskExecutionLinks = new ArrayList<>();

    public HighPriorityComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
    }

    public void add(PriorityComTaskExecutionLink priorityComTaskExecutionLink) {
        priorityComTaskExecutionLinks.add(priorityComTaskExecutionLink);
        super.add(priorityComTaskExecutionLink.getComTaskExecution());
    }

    public List<PriorityComTaskExecutionLink> getPriorityComTaskExecutionLinks() {
        return new ArrayList<>(priorityComTaskExecutionLinks);
    }


    @Override
    public boolean isStillPending() {
        return this.getComServerDAO().areStillPendingWithHighPriority(collectIds(priorityComTaskExecutionLinks));
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
        for (PriorityComTaskExecutionLink comTaskExecution : priorityComTaskExecutionLinks) {
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
