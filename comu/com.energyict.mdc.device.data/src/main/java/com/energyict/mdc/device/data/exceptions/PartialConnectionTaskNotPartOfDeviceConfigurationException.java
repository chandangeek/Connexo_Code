package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.Device;

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

    public PartialConnectionTaskNotPartOfDeviceConfigurationException(Thesaurus thesaurus, PartialConnectionTask partialConnectionTask, Device device) {
        //super(thesaurus, MessageSeeds.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION, partialConnectionTask.getId(), partialConnectionTask.getConfiguration().getDeviceConfiguration().getId(), device.getConfiguration().getId());
        super(thesaurus,
                MessageSeeds.CONNECTION_TASK_PARTIAL_CONNECTION_TASK_NOT_IN_CONFIGURATION,
                partialConnectionTask.getId(),
                partialConnectionTask.getConfiguration().getDeviceConfiguration().getId());
        this.set("partialConnectionTaskId", partialConnectionTask.getId());
        this.set("partialConnectionTaskConfigurationId", partialConnectionTask.getConfiguration().getDeviceConfiguration().getId());
        //this.set("expectedConfigurationId", device.getConfiguration().getId());
        this.set("expectedConfigurationId", 0);
    }

}