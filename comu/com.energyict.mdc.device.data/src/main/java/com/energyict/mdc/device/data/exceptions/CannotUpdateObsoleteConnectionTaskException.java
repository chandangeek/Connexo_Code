package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to update an obsolete {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:17)
 */
public class CannotUpdateObsoleteConnectionTaskException extends LocalizedException {

    public CannotUpdateObsoleteConnectionTaskException(Thesaurus thesaurus, ConnectionTask connectionTask) {
        super(thesaurus, MessageSeeds.CONNECTION_TASK_IS_EXECUTING_AND_CANNOT_OBSOLETE, connectionTask.getName(), connectionTask.getObsoleteDate());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("obsoleteDate", connectionTask.getObsoleteDate());
    }

}