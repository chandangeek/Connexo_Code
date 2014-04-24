package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Models the exceptional situation that occurs when an attempt was made
 * to update a ComTaskExecution that was previously made obsolete
 *
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 11:38
 */
public class CannotUpdateObsoleteComTaskExecutionException extends LocalizedException {

    public CannotUpdateObsoleteComTaskExecutionException(Thesaurus thesaurus, ComTaskExecution comTaskExecution) {
        super(thesaurus, MessageSeeds.COM_TASK_IS_OBSOLETE_AND_CAN_NOT_BE_UPDATED, comTaskExecution.getComTask().getName(), comTaskExecution.getDevice().getName());
        this.set("comTaskExecution", comTaskExecution.getComTask().getName());
        this.set("device", comTaskExecution.getDevice());
    }

}
