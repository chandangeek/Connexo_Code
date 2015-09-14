package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made to make a
 * ComTaskExecution obsolete while the ComServer was currently executing it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-03 (13:25)
 */
public class ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(ComTaskExecution comTaskExecution, ComServer comServer, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comServer.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("comServerName", comServer.getName());
    }

}