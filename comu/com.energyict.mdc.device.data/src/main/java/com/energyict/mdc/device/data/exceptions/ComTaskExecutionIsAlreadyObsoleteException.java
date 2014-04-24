package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Models the exceptional situation that occurs when an attempt is made to make a
 * ComTaskExecution obsolete, when it has already been made obsolete.
 *
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 11:54
 */
public class ComTaskExecutionIsAlreadyObsoleteException extends LocalizedException {

    public ComTaskExecutionIsAlreadyObsoleteException(Thesaurus thesaurus, ComTaskExecution comTaskExecution) {
        super(thesaurus, MessageSeeds.COM_TASK_EXECUTION_IS_ALREADY_OBSOLETE, comTaskExecution.getComTask().getName(), comTaskExecution.getDevice().getName(), comTaskExecution.getObsoleteDate());
        this.set("comTaskExecution", comTaskExecution.getComTask().getName());
        this.set("device", comTaskExecution.getDevice());
        this.set("obsoleteDate", comTaskExecution.getObsoleteDate());
    }
}
