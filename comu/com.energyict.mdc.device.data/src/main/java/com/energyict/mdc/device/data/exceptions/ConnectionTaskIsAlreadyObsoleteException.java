package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to obsolete a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * that is in fact already obsolete.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (11:55)
 */
public class ConnectionTaskIsAlreadyObsoleteException extends LocalizedException {

    public ConnectionTaskIsAlreadyObsoleteException(Thesaurus thesaurus, ConnectionTask<?,?> connectionTask) {
        super(thesaurus, MessageSeeds.CONNECTION_TASK_IS_ALREADY_OBSOLETE, connectionTask.getName(), connectionTask.getDevice().getId(), connectionTask.getObsoleteDate());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("deviceId", connectionTask.getDevice().getId());
        this.set("obsoleteDate", connectionTask.getObsoleteDate());
    }

}