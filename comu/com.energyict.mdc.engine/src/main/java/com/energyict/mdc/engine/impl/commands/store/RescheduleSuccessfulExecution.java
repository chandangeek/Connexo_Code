package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * after its successful execution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleSuccessfulExecution extends RescheduleExecutionDeviceCommand {

    public RescheduleSuccessfulExecution(ScheduledJob scheduledJob) {
        super(scheduledJob);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, ScheduledJob scheduledJob) {
        scheduledJob.reschedule(comServerDAO);
    }

}