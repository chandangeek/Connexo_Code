package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt was made to delete a
 * {@link ComSchedule} from a {@link Device} which was not on that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (15:20)
 */
public class CannotDeleteComScheduleFromDevice extends LocalizedException {

    public CannotDeleteComScheduleFromDevice(Thesaurus thesaurus, ComSchedule comTaskExecution, Device device) {
        super(thesaurus, MessageSeeds.COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE, comTaskExecution.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", device.getName());
    }

}