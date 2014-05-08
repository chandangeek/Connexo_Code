package com.energyict.mdc.engine.impl.core.aspects.performance;

import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.RescheduleBehavior;
import com.energyict.mdc.engine.impl.logging.PerformanceLogger;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.List;

/**
 * Defines pointcuts and advice to monitor the performance of the {@link JobExecution} components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-13 (14:55)
 */
public aspect JobExecutionPerformance {

    private pointcut takeDeviceOffline (JobExecution.ComTaskPreparationContext context):
            execution(private void takeDeviceOffline ())
        && target(context);

    void around (JobExecution.ComTaskPreparationContext context): takeDeviceOffline(context) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("JobExecution.takeDeviceOffline", PerformanceLogger.INSTANCE);
        proceed(context);
        stopWatch.stop();
    }

    private pointcut prepareAll (JobExecution jobExecution, List<? extends ComTaskExecution> comTaskExecutions):
           execution(protected List<JobExecution.PreparedComTaskExecution> prepareAll(List<? extends ComTaskExecution>))
        && target(jobExecution)
        && args(comTaskExecutions);

    List<JobExecution.PreparedComTaskExecution> around (JobExecution jobExecution, List<? extends ComTaskExecution> comTaskExecutions): prepareAll(jobExecution, comTaskExecutions) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("JobExecution.prepareAll", PerformanceLogger.INSTANCE);
        List<JobExecution.PreparedComTaskExecution> preparedComTaskExecutions = proceed(jobExecution, comTaskExecutions);
        stopWatch.stop();
        return preparedComTaskExecutions;
    }

    private pointcut prepareOne (JobExecution jobExecution, ComTaskExecution comTaskExecution):
           execution(protected JobExecution.PreparedComTaskExecution prepareOne(ComTaskExecution))
        && target(jobExecution)
        && args(comTaskExecution);

    JobExecution.PreparedComTaskExecution around (JobExecution jobExecution, ComTaskExecution comTaskExecution): prepareOne(jobExecution, comTaskExecution) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("JobExecution.prepareOne", PerformanceLogger.INSTANCE);
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = proceed(jobExecution, comTaskExecution);
        stopWatch.stop();
        return preparedComTaskExecution;
    }

    private pointcut reschedule (JobExecution jobExecution, RescheduleBehavior.RescheduleReason reason):
           execution(protected void doReschedule (RescheduleBehavior.RescheduleReason))
        && target(jobExecution)
        && args(reason);

    void around (JobExecution jobExecution, RescheduleBehavior.RescheduleReason reason): reschedule(jobExecution, reason) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("JobExecution.doReschedule", PerformanceLogger.INSTANCE);
        proceed(jobExecution, reason);
        stopWatch.stop();
    }

}