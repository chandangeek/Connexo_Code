/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

public class ComTaskExecutionIsAlreadyObsoleteException extends LocalizedException {

    public ComTaskExecutionIsAlreadyObsoleteException(ComTaskExecution comTaskExecution, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comTaskExecution.getObsoleteDate());
        this.set("comTaskExecution", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("obsoleteDate", comTaskExecution.getObsoleteDate());
    }
}
