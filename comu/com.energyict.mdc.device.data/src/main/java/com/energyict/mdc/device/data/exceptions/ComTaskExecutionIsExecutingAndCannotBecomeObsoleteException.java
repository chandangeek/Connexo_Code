package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Models the exceptional situation that occurs when an attempt is made to make a
 * ComTaskExecution obsolete while the ComServer was currently executing it ...
 *
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 11:57
 */
public class ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(Thesaurus thesaurus, ComTaskExecution comTaskExecution, ComServer comServer) {
        super(thesaurus, MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comServer.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("comServerName", comServer.getName());
    }

}
