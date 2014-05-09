package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Provides an implementation for the {@link ComJobFactory} that
 * is intended for single threaded {@link com.energyict.mdc.engine.model.OutboundComPort},
 * i.e. ComPorts that have only 1 simultaneous connections.
 */
public final class SingleThreadedComJobFactory extends GroupingComJobFactory {

    private boolean continueFetching = true;

    public SingleThreadedComJobFactory() {
        super(1);
    }

    @Override
    protected boolean continueFetchingOnNewConnectionTask () {
        return false;
    }

    @Override
    protected boolean continueFetching (ComTaskExecution comTaskExecution) {
        return this.continueFetching && super.continueFetching(comTaskExecution);
    }

    @Override
    protected void addComTaskJob (ServerComTaskExecution comTaskExecution) {
        super.addComTaskJob(comTaskExecution);
        /* As soon as a single ComTaskExecutionJob is added we stop fetching
         * because a single threaded ComPort can only handle a single task at a time. */
        this.continueFetching = false;
     }

}
