/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

import java.util.*;

public abstract class GroupingComJobFactory implements ComJobFactory {

    private int maximumJobs;
    private List<ComJob> jobs = new ArrayList<>();
    private Map<Long, ComTaskExecutionGroup> groups = new HashMap<>();   // grouping based on the ID of the ConnectionTask
    private ComTaskExecution previous;

    public GroupingComJobFactory(int maximumJobs) {
        this.maximumJobs = maximumJobs;
    }

    public int getMaximumJobs() {
        return maximumJobs;
    }

    protected int numberOfJobs() {
        return jobs.size() + groups.values().size();
    }

    @Override
    public List<ComJob> consume(Iterator<ComTaskExecution> comTaskExecutions) {
        while (comTaskExecutions.hasNext()) {
            ComTaskExecution comTaskExecution = comTaskExecutions.next();
            if (continueFetching(comTaskExecution)) {
                add(comTaskExecution);
            } else {
                break;
            }
        }
        jobs.addAll(groups.values());
        return jobs;
    }

    protected boolean continueFetching(ComTaskExecution comTaskExecution) {
        if (needMoreJobs()) {
            if (previous == null) {
                // First ComTaskExecution
                previous = comTaskExecution;
                return true;
            } else {
                /* Continue fetching as long as we are dealing with the
                 * same ConnectionTask and if we are switching to another
                 * ConnectionTask then we only continue fetching
                 * if we support switching to another ConnectionTask. */
                boolean continueFetching = comTaskExecution.usesSameConnectionTaskAs(previous) || continueFetchingOnNewConnectionTask();
                previous = comTaskExecution;
                return continueFetching;
            }
        } else {
            return false;
        }
    }

    protected abstract boolean continueFetchingOnNewConnectionTask();

    private boolean needMoreJobs() {
        return jobs.size() < maximumJobs;
    }

    protected void add(ComTaskExecution comTaskExecution) {
        addToGroup(comTaskExecution);
    }

    protected void addToGroup(ComTaskExecution comTaskExecution) {
        // ComTaskExecution was returned by task query that joins it with the ConnectionTask so group cannot be <code>null</code>
        Optional<ComTaskExecutionGroup> group = this.getComTaskGroup(comTaskExecution);
        group.ifPresent(g -> g.add(comTaskExecution));
    }

    private Optional<ComTaskExecutionGroup> getComTaskGroup(ComTaskExecution comTaskExecution) {
        // ComTaskExecution was returned by task query that joins it with the ConnectionTask so it cannot be <code>null</code>
        Optional<ConnectionTask<?, ?>> connectionTask = comTaskExecution.getConnectionTask();
        if (!connectionTask.isPresent())
            return Optional.empty();
        ComTaskExecutionGroup group = this.groups.get(connectionTask.get().getId());
        if (group == null) {
            group = new ComTaskExecutionGroup(connectionTask.get());
            this.groups.put(connectionTask.get().getId(), group);
        }
        return Optional.of(group);
    }
}
