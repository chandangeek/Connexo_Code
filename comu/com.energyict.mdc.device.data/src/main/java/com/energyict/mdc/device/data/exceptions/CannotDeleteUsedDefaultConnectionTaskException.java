package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to delete a default{@link ConnectionTask}
 * that is still in use by {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:45)
 */
public class CannotDeleteUsedDefaultConnectionTaskException extends LocalizedException {

    public CannotDeleteUsedDefaultConnectionTaskException(Thesaurus thesaurus, ConnectionTask<?,?> connectionTask) {
        super(thesaurus, MessageSeeds.DEFAULT_CONNECTION_TASK_IS_INUSE_AND_CANNOT_DELETE, connectionTask.getName(), connectionTask.getDevice().getId());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("deviceId", connectionTask.getDevice().getId());
    }

}