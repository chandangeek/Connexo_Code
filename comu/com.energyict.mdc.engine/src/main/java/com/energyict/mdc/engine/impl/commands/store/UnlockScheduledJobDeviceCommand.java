package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ScheduledJob;
import com.energyict.mdc.engine.config.ComServer;

/**
 * Models the {@link DeviceCommand} that will unlock a
 * {@link ScheduledJob} after its execution (failed or success).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public class UnlockScheduledJobDeviceCommand extends DeviceCommandImpl {

    private final ScheduledJob scheduledJob;

    public UnlockScheduledJobDeviceCommand(ScheduledJob scheduledJob, ServiceProvider serviceProvider) {
        super(scheduledJob.getComTaskExecutions().stream().findFirst().orElse(null), serviceProvider);
        this.scheduledJob = scheduledJob;
    }

    @Override
    public void executeDuringShutdown(ComServerDAO comServerDAO) {
        this.execute(comServerDAO);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        this.scheduledJob.unlock();
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        PropertyDescriptionBuilder comTasksBuilder = builder.addListProperty("comTaskExecutions");
        this.scheduledJob.getComTaskExecutions()
                .stream()
                .mapToLong(ComTaskExecution::getId)
                .forEach(comTasksBuilder::append);
    }

    @Override
    public String getDescriptionTitle() {
        return "Unlock task";
    }

}