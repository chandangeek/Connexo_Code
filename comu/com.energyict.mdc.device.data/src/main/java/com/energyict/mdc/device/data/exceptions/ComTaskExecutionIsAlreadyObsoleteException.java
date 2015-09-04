package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made to make a
 * ComTaskExecution obsolete, when it has already been made obsolete.
 *
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 11:54
 */
public class ComTaskExecutionIsAlreadyObsoleteException extends LocalizedException {

    public ComTaskExecutionIsAlreadyObsoleteException(ComTaskExecution comTaskExecution, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comTaskExecution.getObsoleteDate());
        this.set("comTaskExecution", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("obsoleteDate", comTaskExecution.getObsoleteDate());
    }
}
