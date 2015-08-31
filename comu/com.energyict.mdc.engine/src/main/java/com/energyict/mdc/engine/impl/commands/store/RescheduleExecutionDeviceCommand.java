package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ScheduledJob;
import com.energyict.mdc.engine.config.ComServer;

/**
 * Provides code reuse opportunities for components that implement
 * the {@link DeviceCommand} interface for the purpose of rescheduling
 * a {@link ScheduledJob} after its execution (failed or success).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public abstract class RescheduleExecutionDeviceCommand extends DeviceCommandImpl {

    private final ScheduledJob scheduledJob;

    public RescheduleExecutionDeviceCommand(ScheduledJob scheduledJob) {
        super(scheduledJob.getComTaskExecutions().stream().findFirst().orElse(null), new NoDeviceCommandServices());
        this.scheduledJob = scheduledJob;
    }

    @Override
    public void executeDuringShutdown(ComServerDAO comServerDAO) {
        this.execute(comServerDAO);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        this.doExecute(comServerDAO, this.scheduledJob);
    }

    protected abstract void doExecute(ComServerDAO comServerDAO, ScheduledJob scheduledJob);

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        PropertyDescriptionBuilder comTasksBuilder = builder.addListProperty("comTaskExecutions");
        this.scheduledJob.getComTaskExecutions()
                .stream()
                .mapToLong(ComTaskExecution::getId)
                .forEach(comTasksBuilder::append);
    }

}