package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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

    public CannotDeleteConnectionTaskWhichIsNotFromThisDevice(ConnectionTask<?, ?> connectionTask, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), device.getName());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("device", device.getName());
    }

}