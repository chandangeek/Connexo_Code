package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.engine.model.ComPortPool;

/**
 * Adds behavior to {@link ConnectionTaskService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (09:40)
 */
public interface ServerConnectionTaskService extends ConnectionTaskService, ReferencePropertySpecFinderProvider {

    /**
     * Tests if the specified {@link ComPortPool} is used
     * by at least one {@link ConnectionTask}.
     *
     * @param comPortPool The ComPortPool
     * @return A flag that indicates if the ComPortPool is used or not
     */
    public boolean hasConnectionTasks(ComPortPool comPortPool);

    /**
     * Tests if the specified {@link PartialConnectionTask} is used
     * by at least one {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @return A flag that indicates if the PartialConnectionTask is used or not
     */
    public boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask);

}