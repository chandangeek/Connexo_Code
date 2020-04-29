/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class GroupingComJobFactory implements ComJobFactory {

    private static final Logger LOGGER = Logger.getLogger(GroupingComJobFactory.class.getName());
    private int maximumJobs;
    private List<ComJob> jobs = new ArrayList<>();
    private Map<Long, ComTaskExecutionGroup> groups = new LinkedHashMap<>();   // grouping based on the ID of the ConnectionTask
    private ComTaskExecution previous;
    private int highestNbOfComTasksInGroup;
    private int nbOfGroupsWithMaxComTasks;

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
        highestNbOfComTasksInGroup = 0;
        nbOfGroupsWithMaxComTasks = 0;
        while (comTaskExecutions.hasNext()) {
            ComTaskExecution comTaskExecution = comTaskExecutions.next();
            if (continueFetching(comTaskExecution)) {
                add(comTaskExecution);
            } else {
                break;
            }
        }
        jobs.addAll(getCompleteJobs());
        return jobs;
    }

    private Collection<ComTaskExecutionGroup> getCompleteJobs() {
        if (groups.isEmpty()) {
            return groups.values();
        }
        Map.Entry<Long, ComTaskExecutionGroup> lastGroup = Iterables.getLast(groups.entrySet());
        countInLastAddedGroup(lastGroup.getValue());
        int pctGroupsWithMaxComTasks = getPercentageOfGroupsWithMaxComTasks();
        if (pctGroupsWithMaxComTasks >= 90 && pctGroupsWithMaxComTasks < 100) {
            // when 90% or more of the groups have the same number of comtasks, then drop the last one, if it's incomplete
            if (isLastGroupIncomplete(lastGroup)) {
                removeLastGroup(lastGroup);
            }
            LOGGER.info("perf - " + (groups.size() - nbOfGroupsWithMaxComTasks) + " incomplete jobs out of " + groups.size());
        }
        return groups.values();
    }

    private void countInLastAddedGroup(ComTaskExecutionGroup comTaskExecutionGroup) {
        if (comTaskExecutionGroup.getComTaskExecutions().size() == highestNbOfComTasksInGroup) {
            ++nbOfGroupsWithMaxComTasks;
        }
    }

    private int getPercentageOfGroupsWithMaxComTasks() {
        // groups already checked for emptiness
        return nbOfGroupsWithMaxComTasks * 100 / groups.size();
    }

    private boolean isLastGroupIncomplete(Map.Entry<Long, ComTaskExecutionGroup> lastGroup) {
        return lastGroup.getValue().getComTaskExecutions().size() < highestNbOfComTasksInGroup;
    }

    private void removeLastGroup(Map.Entry<Long, ComTaskExecutionGroup> lastGroup) {
        groups.remove(lastGroup.getKey());
    }

    @Override
    public boolean consume(ComTaskExecution comTaskExecution) {
        if (comTaskExecution != null && continueFetching(comTaskExecution)) {
            add(comTaskExecution);
            return true;
        }
        return false;
    }

    @Override
    public List<ComJob> getJobs() {
        return new ArrayList<>(groups.values());
    }

    @Override
    public List<ComJob> collect(Iterator<ComTaskExecution> comTaskExecutions) {
        while (comTaskExecutions.hasNext()) {
            add(comTaskExecutions.next());
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
            // does it ever happen?
            LOGGER.warning("perf - needMoreJobs was false");
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
            if (!groups.isEmpty()) {
                updateIncompleteJobCounters();
            }
            group = new ComTaskExecutionGroup(connectionTask.get());
            this.groups.put(connectionTask.get().getId(), group);
        }
        return Optional.of(group);
    }

    private void updateIncompleteJobCounters() {
        int nbOfComTasksInLastGroup = Iterables.getLast(groups.entrySet()).getValue().getComTaskExecutions().size();
        if (nbOfComTasksInLastGroup > highestNbOfComTasksInGroup) {
            highestNbOfComTasksInGroup = nbOfComTasksInLastGroup;
            nbOfGroupsWithMaxComTasks = 1;
        } else {
            if (nbOfComTasksInLastGroup == highestNbOfComTasksInGroup) {
                ++nbOfGroupsWithMaxComTasks;
            }
        }
    }
}
