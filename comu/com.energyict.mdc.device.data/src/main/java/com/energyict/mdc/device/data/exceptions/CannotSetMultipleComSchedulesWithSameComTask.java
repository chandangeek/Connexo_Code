/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.scheduling.model.ComSchedule;

/**
 * Models the exceptional behavior that occurs when you try to add a ComSchedule to
 * a device while one of its ComTaskExecutions was already linked to another ComSchedule
 */
public class CannotSetMultipleComSchedulesWithSameComTask extends LocalizedException {

    public CannotSetMultipleComSchedulesWithSameComTask(ComSchedule comTaskExecution, Device device, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.MULTIPLE_COMSCHEDULES_WITH_SAME_COMTASK);
        this.set("comTaskexecution", comTaskExecution.getId());
        this.set("device", device.getName());
    }
}
