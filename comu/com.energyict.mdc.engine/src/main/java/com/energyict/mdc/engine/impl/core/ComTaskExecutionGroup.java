package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ComJob} interface
 * that represents a group of {@link ComTaskExecution}s that need to be
 * executed one after the other.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-20 (14:06)
 */
public class ComTaskExecutionGroup implements ComJob {

    private ScheduledConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();

    public ComTaskExecutionGroup (ScheduledConnectionTask connectionTask) {
        super();
        this.connectionTask = connectionTask;
    }

    @Override
    public boolean isGroup () {
        return true;
    }

    @Override
    public ScheduledConnectionTask getConnectionTask () {
        return this.connectionTask;
    }

    public void add (ComTaskExecution comTask) {
        this.comTaskExecutions.add(comTask);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions () {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.addAll(this.comTaskExecutions);
        return comTaskExecutions;
    }

}