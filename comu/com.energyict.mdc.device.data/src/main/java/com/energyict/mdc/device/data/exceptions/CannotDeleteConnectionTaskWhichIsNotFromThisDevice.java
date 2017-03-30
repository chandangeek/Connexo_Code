/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

public class CannotDeleteConnectionTaskWhichIsNotFromThisDevice extends LocalizedException {

    public CannotDeleteConnectionTaskWhichIsNotFromThisDevice(ConnectionTask<?, ?> connectionTask, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), device.getName());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("device", device.getName());
    }

}