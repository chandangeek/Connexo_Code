package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionCompletionEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionFailureEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionStartedEvent;

/**
 * Defines pointcuts and advice that will publish events
 * that relate to the execution of {@link com.energyict.mdc.tasks.ComTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:21)
 */
public aspect OutboundComTaskEventPublisher {

    private pointcut startTask (JobExecution job, ComTaskExecution comTaskExecution):
            execution(void JobExecution.start(ComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    after (JobExecution job, ComTaskExecution comTaskExecution): startTask(job, comTaskExecution) {
        this.publish(
                new ComTaskExecutionStartedEvent(
                        comTaskExecution,
                        job.getExecutionContext().getComPort(),
                        job.getExecutionContext().getConnectionTask(),
                        EventPublisherImpl.getInstance().serviceProvider()));
    }

    private pointcut completeTask (JobExecution job, ComTaskExecution comTaskExecution):
            execution(void JobExecution.completeExecutedComTask(ComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    after (JobExecution job, ComTaskExecution comTaskExecution): completeTask(job, comTaskExecution) {
        this.publish(
                new ComTaskExecutionCompletionEvent(
                        comTaskExecution,
                        job.getExecutionContext().getComPort(),
                        job.getExecutionContext().getConnectionTask(),
                        EventPublisherImpl.getInstance().serviceProvider()));
    }

    private pointcut taskFailure (JobExecution job, ComTaskExecution comTaskExecution, Throwable cause):
            execution(void JobExecution.failure(ComTaskExecution, java.lang.Throwable))
         && target(job)
         && args(comTaskExecution, cause);

    before (JobExecution job, ComTaskExecution comTaskExecution, Throwable cause): taskFailure(job, comTaskExecution, cause) {
        this.publish(
                new ComTaskExecutionFailureEvent(
                        comTaskExecution,
                        job.getExecutionContext().getComPort(),
                        job.getExecutionContext().getConnectionTask(),
                        cause,
                        EventPublisherImpl.getInstance().serviceProvider()));
    }

    private pointcut executeTask (JobExecution job, JobExecution.PreparedComTaskExecution comTaskExecution):
            execution(boolean JobExecution.execute(JobExecution.PreparedComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    after (JobExecution job, JobExecution.PreparedComTaskExecution preparedComTaskExecution) returning (boolean success) : executeTask(job, preparedComTaskExecution) {
        if (!success) {
            this.publish(
                    new ComTaskExecutionFailureEvent(
                            preparedComTaskExecution.getComTaskExecution(),
                            job.getExecutionContext().getComPort(),
                            job.getExecutionContext().getConnectionTask(),
                            EventPublisherImpl.getInstance().serviceProvider()));
        }
    }

    after (JobExecution job, JobExecution.PreparedComTaskExecution preparedComTaskExecution) throwing (Throwable cause) : executeTask(job, preparedComTaskExecution) {
        this.publish(
                new ComTaskExecutionFailureEvent(
                        preparedComTaskExecution.getComTaskExecution(),
                        job.getExecutionContext().getComPort(),
                        job.getExecutionContext().getConnectionTask(),
                        cause,
                        EventPublisherImpl.getInstance().serviceProvider()));
    }

    private void publish (ComServerEvent event) {
        EventPublisherImpl.getInstance().publish(event);
    }

}