/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.scheduling.ComSchedule;

/**
 * Models the exceptional situation that occurs when an attempt was made to delete a
 * {@link ComSchedule} from a {@link Device} which was not on that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (15:20)
 */
public class CannotDeleteComScheduleFromDevice extends LocalizedException {

    public CannotDeleteComScheduleFromDevice(ComSchedule comTaskExecution, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, comTaskExecution.getName());
        this.set("comTask", comTaskExecution.getId());
        this.set("device", device.getName());
    }

}