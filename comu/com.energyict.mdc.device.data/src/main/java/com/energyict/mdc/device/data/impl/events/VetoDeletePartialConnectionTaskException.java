package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link PartialConnectionTask}
 * is being deleted while it is still being used by one or more
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-09 (14:31)
 */
public class VetoDeletePartialConnectionTaskException extends LocalizedException {

    protected VetoDeletePartialConnectionTaskException(Thesaurus thesaurus, PartialConnectionTask partialConnectionTask) {
        super(thesaurus, MessageSeeds.VETO_PARTIAL_CONNECTION_TASK_DELETION, partialConnectionTask.getName(), partialConnectionTask.getConfiguration().getName());
        this.set("partialConnectionTaskId", partialConnectionTask.getId());
        this.set("deviceConfigurationId", partialConnectionTask.getConfiguration().getId());
    }

}