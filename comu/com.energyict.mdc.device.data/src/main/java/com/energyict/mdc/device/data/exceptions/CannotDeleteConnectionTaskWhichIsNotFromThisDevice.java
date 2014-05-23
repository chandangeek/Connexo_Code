package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to delete a ConnectionTask which is
 * not owned by the device
 *
 * Copyrights EnergyICT
 * Date: 09/04/14
 * Time: 11:18
 */
public class CannotDeleteConnectionTaskWhichIsNotFromThisDevice extends LocalizedException {

    public CannotDeleteConnectionTaskWhichIsNotFromThisDevice(Thesaurus thesaurus, ConnectionTask<?,?> connectionTask, Device device) {
        super(thesaurus, MessageSeeds.CONNECTION_TASK_CANNOT_DELETE_IF_NOT_FROM_DEVICE, connectionTask.getName(), device.getName());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("device", device.getName());
    }
}
