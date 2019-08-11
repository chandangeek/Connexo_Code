/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to delete a default{@link ConnectionTask}
 * that is still in use by {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:45)
 */
public class CannotDeleteUsedDefaultConnectionTaskException extends LocalizedException {

    public CannotDeleteUsedDefaultConnectionTaskException(ConnectionTask<?, ?> connectionTask, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), connectionTask.getDevice().getId());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("deviceId", connectionTask.getDevice().getId());
    }

}