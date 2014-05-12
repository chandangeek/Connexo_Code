package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.tasks.ComTask;

public interface AdHocComTaskExecution extends ComTaskExecution {
    /**
     * Gets the {@link ComTask} that specifies
     * the details of this ComTaskExecution.
     *
     * @return The ComTask
     */
    public ComTask getComTask ();

}
