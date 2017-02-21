/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

public class CannotDeleteComTaskExecutionWhichIsNotFromThisDevice extends LocalizedException {

    public CannotDeleteComTaskExecutionWhichIsNotFromThisDevice(ComTaskExecution comTaskExecution, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), device.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", device.getName());
    }

}