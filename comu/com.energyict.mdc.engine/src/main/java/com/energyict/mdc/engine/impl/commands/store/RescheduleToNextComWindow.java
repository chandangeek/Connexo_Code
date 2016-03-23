package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * because the current timestamp is not within the {@link com.energyict.mdc.common.ComWindow}
 * of the related {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleToNextComWindow extends RescheduleExecutionDeviceCommand {

    private final static String DESCRIPTION_TITLE = "Reschedule to next communication window";

    public RescheduleToNextComWindow(ScheduledJob scheduledJob) {
        super(scheduledJob);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, ScheduledJob scheduledJob) {
        scheduledJob.rescheduleToNextComWindow(comServerDAO);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE ;
    }

}