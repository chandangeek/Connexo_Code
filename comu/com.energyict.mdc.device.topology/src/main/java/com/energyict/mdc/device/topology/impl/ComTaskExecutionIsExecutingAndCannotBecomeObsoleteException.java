/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;

/**
 * Models the exceptional situation that occurs when an attempt is made to make a
 * ComTaskExecution obsolete while the ComServer was currently executing it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-03 (13:25)
 */
public class ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(ComTaskExecution comTaskExecution, ComPort comPort, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), comTaskExecution.getDevice().getName(), comPort.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", comTaskExecution.getDevice());
        this.set("comPort", comPort.getName());
    }

}