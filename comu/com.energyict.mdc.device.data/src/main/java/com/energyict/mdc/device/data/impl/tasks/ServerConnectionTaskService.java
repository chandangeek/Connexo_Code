/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import java.util.List;

/**
 * Adds behavior to {@link ConnectionTaskService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (09:40)
 */
public interface ServerConnectionTaskService extends ConnectionTaskService {

    /**
     * Tests if the specified {@link ComPortPool} is used
     * by at least one {@link ConnectionTask}.
     *
     * @param comPortPool The ComPortPool
     * @return A flag that indicates if the ComPortPool is used or not
     */
    boolean hasConnectionTasks(ComPortPool comPortPool);

    /**
     * Tests if the specified {@link PartialConnectionTask} is used
     * by at least one {@link ConnectionTask}.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @return A flag that indicates if the PartialConnectionTask is used or not
     */
    boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask);

    /**
     * Finds and returns the unique identifiers of all{@link ConnectionTask}s
     * that use the {@link PartialConnectionTask}
     * that is uniquely identified by the specified number.
     *
     * @param partialConnectionTaskId The unique identifier of the PartialConnectionTask
     * @return The List of ConnectionTask
     */
    List<Long> findConnectionTasksForPartialId(long partialConnectionTaskId);

}