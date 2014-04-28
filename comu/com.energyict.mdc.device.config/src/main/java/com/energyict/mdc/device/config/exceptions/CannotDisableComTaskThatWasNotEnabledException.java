package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.tasks.ComTask;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to disable a {@link com.energyict.mdc.tasks.ComTask}
 * that was not enabled on the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * in the first place.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-22 (15:31)
 */
public class CannotDisableComTaskThatWasNotEnabledException extends LocalizedException {

    public CannotDisableComTaskThatWasNotEnabledException(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, ComTask comTask) {
        super(thesaurus, MessageSeeds.COM_TASK_ENABLEMENT_DOES_NOT_EXIST, comTask.getName(), deviceConfiguration.getName());
        this.set("comTask", comTask);
        this.set("deviceConfiguration", deviceConfiguration);
    }

}