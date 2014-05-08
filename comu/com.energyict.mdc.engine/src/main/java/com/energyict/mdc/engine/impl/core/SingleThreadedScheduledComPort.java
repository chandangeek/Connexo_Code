package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.issues.IssueService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface
 * for an {@link OutboundComPort} that supports one connection at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:30)
 */
public class SingleThreadedScheduledComPort extends ScheduledComPortImpl {

    private ScheduledJobTransactionExecutor transactionExecutor;

    public SingleThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        super(comPort, comServerDAO, deviceCommandExecutor, issueService);
        this.transactionExecutor = new ScheduledJobTransactionExecutorDefaultImplementation();
    }

    public SingleThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, IssueService issueService) {
        this(comPort, comServerDAO, deviceCommandExecutor, new ScheduledJobTransactionExecutorDefaultImplementation(), threadFactory, issueService);
    }

    public SingleThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledJobTransactionExecutor transactionExecutor, ThreadFactory threadFactory, IssueService issueService) {
        super(comPort, comServerDAO, deviceCommandExecutor, threadFactory, issueService);
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    protected void doRun () {
        this.executeTasks();
        this.checkAndApplyChanges();
    }

    @Override
    protected JobScheduler getJobScheduler () {
        return new SingleThreadedJobScheduler(this.getComPort().getComServer().getCommunicationLogLevel());
    }

    /**
     * Organizes a Collection of {@link ComTaskExecution}s
     * into a Collection of {@link com.energyict.comserver.core.ComTaskExecutionGroup}s.
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
        public int scheduleAll(List<ComJob> jobs) {
            for (ScheduledJob job : this.toScheduledJobs(jobs)) {
                new SingleThreadedScheduledJobExecutor(transactionExecutor, this.logLevel, getDeviceCommandExecutor()).acquireTokenAndPerformSingleJob(job);
            }
            return jobs.size();
        }

        private List<ScheduledJob> toScheduledJobs (List<ComJob> jobs) {
            List<ScheduledJob> scheduledJobs = new ArrayList<>();
            for (ComJob job : jobs) {
                if (job.isGroup()) {
                    scheduledJobs.add(newComTaskGroup(job));
                }
                else {
                    List<ComTaskExecution> scheduledComTasks = job.getComTaskExecutions();
                    scheduledJobs.add(newComTaskJob(scheduledComTasks.get(0)));
                }
            }
            return scheduledJobs;
        }

    }

}