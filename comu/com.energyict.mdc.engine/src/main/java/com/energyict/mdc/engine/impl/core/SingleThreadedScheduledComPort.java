package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import java.util.Optional;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface
 * for an {@link OutboundComPort} that supports one connection at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:30)
 */
public class SingleThreadedScheduledComPort extends ScheduledComPortImpl {

    public SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    public SingleThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, threadFactory, serviceProvider);
    }

    @Override
    protected void setThreadPrinciple() {
        Optional<User> user = getServiceProvider().userService().findUser(EngineServiceImpl.COMSERVER_USER);
        if (user.isPresent()) {
            getServiceProvider().threadPrincipalService().set(user.get(), "SingleThreadedComPort", "Executing", Locale.ENGLISH);
        }
    }

    @Override
    protected JobScheduler getJobScheduler () {
        return new SingleThreadedJobScheduler(this.getComPort().getComServer().getCommunicationLogLevel());
    }

    /**
     * Organizes a Collection of {@link ComTaskExecution}s
     * into a Collection of ComTaskExecutionGroups.
     */
    private class SingleThreadedJobScheduler implements JobScheduler, ScheduledJobExecutionEventListener {

        private ComServer.LogLevel logLevel;

        private SingleThreadedJobScheduler (ComServer.LogLevel logLevel) {
            super();
            this.logLevel = logLevel;
        }

        @Override
        public void executionStarted (ScheduledJob job) {
            // I know because I kicked the execute method myself
        }

        @Override
        public void scheduleAll(List<ComJob> jobs) {
            for (ScheduledJob job : this.toScheduledJobs(jobs)) {
                new SingleThreadedScheduledJobExecutor(getServiceProvider().transactionService(), this.logLevel, getDeviceCommandExecutor()).acquireTokenAndPerformSingleJob(job);
            }
        }

        private List<ScheduledJob> toScheduledJobs (List<ComJob> jobs) {
            return jobs.stream().map(SingleThreadedScheduledComPort.this::newComTaskGroup).collect(Collectors.toList());
        }

    }

}