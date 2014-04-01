package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.device.Device;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to create a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * against a Device for a {@link com.energyict.mdc.device.config.PartialConnectionTask}
 * for which another ConnectionTask already exists on the same Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (11:04)
 */
public class DuplicateConnectionTaskException extends LocalizedException {

    public DuplicateConnectionTaskException(Thesaurus thesaurus, Device device, PartialConnectionTask partialConnectionTask, ConnectionTask existingConnectionTask) {
        super(thesaurus, MessageSeeds.DUPLICATE_CONNECTION_TASK, partialConnectionTask.getName(), existingConnectionTask.getName(), device.getId());
        this.set("device", device);
        this.set("partialConnectionTask", partialConnectionTask);
        this.set("existingConnectionTask", existingConnectionTask);
    }

}