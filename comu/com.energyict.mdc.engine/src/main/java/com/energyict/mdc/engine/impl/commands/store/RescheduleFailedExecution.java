/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * after a failed execution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleFailedExecution extends RescheduleExecutionDeviceCommand {

    private static final String DESCRIPTION_TITLE = "Reschedule after failure";


    public RescheduleFailedExecution(JobExecution scheduledJob) {
        super(scheduledJob);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, JobExecution scheduledJob) {
        scheduledJob.doReschedule();
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}