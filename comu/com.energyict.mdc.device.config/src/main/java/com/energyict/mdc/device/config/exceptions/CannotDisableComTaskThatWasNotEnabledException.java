/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.tasks.ComTask;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link ComTask}
 * that was not enabled on the {@link DeviceConfiguration}
 * in the first place.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-22 (15:31)
 */
public class CannotDisableComTaskThatWasNotEnabledException extends LocalizedException {

    public CannotDisableComTaskThatWasNotEnabledException(DeviceConfiguration deviceConfiguration, ComTask comTask, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTask.getName(), deviceConfiguration.getName());
        this.set("comTask", comTask);
        this.set("deviceConfiguration", deviceConfiguration);
    }

}