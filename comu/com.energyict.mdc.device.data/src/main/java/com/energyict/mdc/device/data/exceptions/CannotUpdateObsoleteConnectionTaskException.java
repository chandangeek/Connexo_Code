/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to update an obsolete {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:17)
 */
public class CannotUpdateObsoleteConnectionTaskException extends LocalizedException {

    public CannotUpdateObsoleteConnectionTaskException(ConnectionTask<?, ?> connectionTask, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), connectionTask.getObsoleteDate());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("obsoleteDate", connectionTask.getObsoleteDate());
    }

}