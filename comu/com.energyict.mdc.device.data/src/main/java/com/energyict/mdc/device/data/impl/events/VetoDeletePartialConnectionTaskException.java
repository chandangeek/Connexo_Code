/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptional situation that occurs when a {@link PartialConnectionTask}
 * is being deleted while it is still being used by one or more
 * {@link ConnectionTask}s.
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