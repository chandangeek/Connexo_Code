/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;

public class ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(ComTaskExecution comTaskExecution, ComPort comPort, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comPort.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("comPort", comPort.getName());
    }

}
