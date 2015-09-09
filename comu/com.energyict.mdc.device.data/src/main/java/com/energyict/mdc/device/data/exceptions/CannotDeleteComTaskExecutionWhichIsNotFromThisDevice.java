package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt was made to delete a
 * {@link ComTaskExecution} from a {@link Device} which was not owned by that Device.
 *
 * Copyrights EnergyICT
 * Date: 15/04/14
 * Time: 13:57
 */
public class CannotDeleteComTaskExecutionWhichIsNotFromThisDevice extends LocalizedException {

    public CannotDeleteComTaskExecutionWhichIsNotFromThisDevice(ComTaskExecution comTaskExecution, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getId(), device.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", device.getName());
    }

}