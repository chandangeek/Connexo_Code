/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;

public class ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(ComTaskExecution comTaskExecution, ComServer comServer, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comServer.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("comServerName", comServer.getName());
    }

}
