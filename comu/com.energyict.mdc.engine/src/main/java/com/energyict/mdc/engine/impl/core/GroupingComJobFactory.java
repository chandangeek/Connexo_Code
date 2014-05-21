package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 09/05/14
 * Time: 14:48
 */
public abstract class GroupingComJobFactory implements ComJobFactory {

    private int maximumJobs;
    private List<ComJob> jobs = new ArrayList<>();
    private Map<OutboundConnectionTask, ComTaskExecutionGroup> groups = new HashMap<>();
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
                this.add((ServerComTaskExecution) comTaskExecution);
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

    protected void add(ServerComTaskExecution comTaskExecution) {
        ScheduledConnectionTask connectionTask = (ScheduledConnectionTask) comTaskExecution.getConnectionTask();
        if (this.supportsSimultaneousConnections(connectionTask)) {
            this.addComTaskJob(comTaskExecution);
        } else {
            this.addToGroup(comTaskExecution);
        }
    }

    /**
     * Tests if the {@link com.energyict.mdc.device.data.tasks.ConnectionTask} supports simultaneous connections.
     *
     * @param connectionTask The ConnectionTask
     * @return A flag that indicates if the ComTaskExecution can be run in isolation
     */
    private boolean supportsSimultaneousConnections(ScheduledConnectionTask connectionTask) {
        return ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(connectionTask.getConnectionStrategy())
                && connectionTask.isSimultaneousConnectionsAllowed()
                && connectionTask.getConnectionType().allowsSimultaneousConnections();

    }

    protected void addComTaskJob(ServerComTaskExecution scheduledComTask) {
        this.jobs.add(new ComTaskExecutionJob(scheduledComTask));
    }

    protected void addToGroup(ServerComTaskExecution comTaskExecution) {
        ComTaskExecutionGroup group = this.getComTaskGroup(comTaskExecution);
        group.add(comTaskExecution);
    }

    private ComTaskExecutionGroup getComTaskGroup(ServerComTaskExecution comTaskExecution) {
        OutboundConnectionTask connectionTask = (OutboundConnectionTask) comTaskExecution.getConnectionTask();
        ComTaskExecutionGroup group = this.groups.get(connectionTask);
        if (group == null) {
            group = new ComTaskExecutionGroup(connectionTask);
            this.groups.put(connectionTask, group);
        }
        return group;
    }
}
