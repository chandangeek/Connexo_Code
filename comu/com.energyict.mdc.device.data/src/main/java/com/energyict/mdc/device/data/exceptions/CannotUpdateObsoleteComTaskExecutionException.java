package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt was made
 * to update a ComTaskExecution that was previously made obsolete
 *
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 11:38
 */
public class CannotUpdateObsoleteComTaskExecutionException extends LocalizedException {

    public CannotUpdateObsoleteComTaskExecutionException(ComTaskExecution comTaskExecution, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName());
        this.set("comTaskExecution", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
    }

}
