package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionFailureReason;
import com.energyict.mdc.engine.impl.core.RescheduleBehavior;
import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Models a {@link DeviceCommand} that reschedules a {@link ScheduledJob}
 * after a failed execution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class RescheduleFailedExecution extends RescheduleExecutionDeviceCommand {

    private static final String DESCRIPTION_TITLE =  "Reschedule after failure";

    private final Throwable failure;
    private final RescheduleBehavior.RescheduleReason rescheduleReason;

    public RescheduleFailedExecution(ScheduledJob scheduledJob, Throwable failure, ExecutionFailureReason reason) {
        super(scheduledJob);
        this.failure = failure;
        this.rescheduleReason = reason.toRescheduleReason();
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, ScheduledJob scheduledJob) {
        scheduledJob.reschedule(comServerDAO, this.failure, this.rescheduleReason);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}