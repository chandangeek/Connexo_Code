/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.comserver.ComPort;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Created by Jozsef Szekrenyes on 06/02/2019.
 */
@ProviderType
public interface PriorityComTaskExecutionLink extends HasId {

    /**
     * Gets the {@link ComTaskExecution} that should be executed with high priority.
     *
     * @return The ComTaskExecution
     */
    ComTaskExecution getComTaskExecution();

    /**
     * Gets the {@link ConnectionTask} which will be used
     * to perform the {@link ComTaskExecution}.
     *
     * @return the used ConnectionTask
     */
    ConnectionTask getConnectionTask();

    /**
     * Gets the earliest possible timestamp on which this HighPriorityComTaskExecution
     * will effectively be executed by the ComServer.
     *
     * @return The earliest possible next execution timestamp
     */
    Instant getNextExecutionTimestamp();

    /**
     * Tests if this HighPriorityComTaskExecution is currently executing.
     * Convenience (and possibly faster) for <code>getExecutingComPort() != null</code>.
     *
     * @return <code>true</code> iff this HighPriorityComTaskExecution is executing,
     *         i.e. if the executing ComPort is not null
     */
    boolean isExecuting();

    ComPort getExecutingComPort();

    /**
     * Gets this HighPriorityComTaskExecution's execution priority.
     * Remember that this is a positive number
     * and smaller numbers indicate higher priority.
     * Zero is therefore the absolute highest priority.
     *
     * @return The execution priority
     */
    int getPriority();

    void save();
}
