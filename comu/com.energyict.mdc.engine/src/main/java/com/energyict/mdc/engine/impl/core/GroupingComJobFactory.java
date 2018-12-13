/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class GroupingComJobFactory implements ComJobFactory {

    private int maximumJobs;
    private List<ComJob> jobs = new ArrayList<>();
    private Map<Long, ComTaskExecutionGroup> groups = new HashMap<>();   // grouping based on the ID of the ConnectionTask
    private ComTaskExecution previous;

    public GroupingComJobFactory(int maximumJobs) {
        super();
        this.maximumJobs = maximumJobs;
    }

    public int getMaximumJobs() {
        return maximumJobs;
    }

    protected int numberOfJobs() {
        return this.jobs.size() + this.groups.values().size();
    }

    @Override
    public List<ComJob> consume(Iterator<ComTaskExecution> comTaskExecutions) {
        while (comTaskExecutions.hasNext()) {
            ComTaskExecution comTaskExecution = comTaskExecutions.next();
            if (continueFetching(comTaskExecution)) {
                this.add(comTaskExecution);
            } else {
                break;
            }
        }
        this.jobs.addAll(this.groups.values());
        return this.jobs;
    }

    protected boolean continueFetching(ComTaskExecution comTaskExecution) {
        if (this.needMoreJobs()) {
            if (this.previous == null) {
                // First ComTaskExecution
                this.previous = comTaskExecution;
                return true;
            } else {
                /* Continue fetching as long as we are dealing with the
                 * same ConnectionTask and if we are switching to another
                 * ConnectionTask then we only continue fetching
                 * if we support switching to another ConnectionTask. */
                boolean continueFetching = comTaskExecution.usesSameConnectionTaskAs(this.previous) || this.continueFetchingOnNewConnectionTask();
                this.previous = comTaskExecution;
                return continueFetching;
            }
        } else {
            return false;
        }
    }

    protected abstract boolean continueFetchingOnNewConnectionTask();

    private boolean needMoreJobs() {
        return this.jobs.size() < this.maximumJobs;
    }

    protected void add(ComTaskExecution comTaskExecution) {
        this.addToGroup(comTaskExecution);
    }

    protected void addToGroup(ComTaskExecution comTaskExecution) {
        ComTaskExecutionGroup group = this.getComTaskGroup(comTaskExecution);
        group.add(comTaskExecution);
    }

    private ComTaskExecutionGroup getComTaskGroup(ComTaskExecution comTaskExecution) {
        // ComTaskExecution was returned by task query that joins it with the ConnectionTask so it cannot be <code>null</code>
        long connectionTaskId = comTaskExecution.getConnectionTaskId();
        ComTaskExecutionGroup group = this.groups.get(connectionTaskId);
        if (group == null) {
            group = new ComTaskExecutionGroup(connectionTaskId);
            this.groups.put(connectionTaskId, group);
        }
        return group;
    }
}
