/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to create a {@link ConnectionTask}
 * against a Device for a {@link PartialConnectionTask}
 * that is not part of the device's configuration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (11:40)
 */
public class PartialConnectionTaskNotPartOfDeviceConfigurationException extends LocalizedException {

    public PartialConnectionTaskNotPartOfDeviceConfigurationException(PartialConnectionTask partialConnectionTask, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus,
                messageSeed,
                partialConnectionTask.getId(),
                partialConnectionTask.getConfiguration().getId());
        this.set("partialConnectionTaskId", partialConnectionTask.getId());
        this.set("partialConnectionTaskConfigurationId", partialConnectionTask.getConfiguration().getId());
        this.set("expectedConfigurationId", device.getDeviceConfiguration().getId());
    }

}