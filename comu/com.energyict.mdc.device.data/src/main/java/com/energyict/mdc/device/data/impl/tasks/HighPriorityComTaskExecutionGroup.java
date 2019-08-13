/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HighPriorityComTaskExecutionGroup implements HighPriorityComJob {

    private ScheduledConnectionTask connectionTask;
    private List<PriorityComTaskExecutionLink> priorityComTaskExecutionLinks = new ArrayList<>();

    HighPriorityComTaskExecutionGroup(ScheduledConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    public void add(PriorityComTaskExecutionLink priorityComTask) {
        priorityComTaskExecutionLinks.add(priorityComTask);
    }

    public void addAll(List<PriorityComTaskExecutionLink> priorityComTasks) {
        priorityComTaskExecutionLinks.addAll(priorityComTasks);
    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        return connectionTask;
    }

    @Override
    public List<PriorityComTaskExecutionLink> getPriorityComTaskExecutionLinks() {
        return ImmutableList.copyOf(priorityComTaskExecutionLinks);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return priorityComTaskExecutionLinks.stream()
                .map(PriorityComTaskExecutionLink::getComTaskExecution)
                .filter(cte -> cte != null)
                .collect(Collectors.toList());
    }
}
