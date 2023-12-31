/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJob;

/**
 * Provides code reuse opportunities for components that implement
 * the {@link DeviceCommand} interface for the purpose of rescheduling
 * a {@link ScheduledJob} after its execution (failed or success).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-23 (15:32)
 */
public abstract class RescheduleExecutionDeviceCommand extends DeviceCommandImpl {
    private final JobExecution scheduledJob;

    public RescheduleExecutionDeviceCommand(JobExecution scheduledJob, ServiceProvider serviceProvider) {
        super(scheduledJob.getComTaskExecutions().stream().findFirst().orElse(null), serviceProvider);
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

    protected abstract void doExecute(ComServerDAO comServerDAO, JobExecution scheduledJob);

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        PropertyDescriptionBuilder comTasksBuilder = builder.addListProperty("comTaskExecutions");
        this.scheduledJob.getComTaskExecutions()
                .stream()
                .mapToLong(ComTaskExecution::getId)
                .forEach(comTasksBuilder::append);
    }
}
