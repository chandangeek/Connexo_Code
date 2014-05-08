package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.engine.impl.core.aspects.statistics.AbstractCommunicationStatisticsMonitor;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.comserver.time.StopWatch;
import com.energyict.comserver.tools.Counters;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.shadow.journal.ComStatisticsShadow;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Defines pointcuts and advice to monitor the execution time
 * of {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (14:49)
 */
public privileged aspect OutboundCommunicationStatisticsMonitor extends AbstractCommunicationStatisticsMonitor {
    declare precedence:
            CommunicationStatisticsMonitor,
            com.energyict.comserver.aspects.logging.ComChannelReadWriteLogger,
            com.energyict.comserver.aspects.events.ComChannelReadWriteEventPublisher;

    private StopWatch JobExecution.ExecutionContext.connecting;
    private StopWatch JobExecution.ExecutionContext.executing;

    private pointcut establishConnectionFor (JobExecution.ExecutionContext executionContext):
            execution(public boolean JobExecution.ExecutionContext.connect())
                    && target(executionContext);

    before (JobExecution.ExecutionContext executionContext): establishConnectionFor(executionContext) {
        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
    }

    after (JobExecution.ExecutionContext executionContext): establishConnectionFor(executionContext) {
        if (this.isConnected(executionContext)) {
            executionContext.connecting.stop();
        }
    }

    private pointcut closeConnection (JobExecution.ExecutionContext executionContext):
            execution(public void JobExecution.ExecutionContext.close())
                    && target(executionContext);

    after (JobExecution.ExecutionContext executionContext): closeConnection(executionContext) {
        /* closeConnection is called from finally block
         * even when the connection was never established.
         * So first test if there was a connection. */
        ComSessionShadow comSessionShadow = executionContext.getComSessionShadow();
        if (this.isConnected(executionContext)) {
            ComPortRelatedComChannelImpl comChannel = (ComPortRelatedComChannelImpl) executionContext.getComChannel();
            StopWatch talking = this.getComChannelTalkCounter(comChannel);
            talking.stop();
            comSessionShadow.setConnectMillis(executionContext.connecting.getTotalElapsedTime());
            comSessionShadow.setTalkMillis(talking.getTotalElapsedTime());
            Counters sessionCounters = this.getComChannelSessionCounters(comChannel);
            comSessionShadow.getComStatistics().setNrOfBytesSent(sessionCounters.getBytesSent());
            comSessionShadow.getComStatistics().setNrOfBytesRead(sessionCounters.getBytesRead());
            comSessionShadow.getComStatistics().setNrOfPacketsSent(sessionCounters.getPacketsSent());
            comSessionShadow.getComStatistics().setNrOfPacketsRead(sessionCounters.getPacketsRead());
        }
    }

    private boolean isConnected (JobExecution.ExecutionContext executionContext) {
        return executionContext.getComChannel() != null;
    }

    private pointcut comTaskExecutionStarts (JobExecution scheduledJob, ComTaskExecution comTaskExecution):
            execution(void JobExecution.start(ComTaskExecution))
                    && target(scheduledJob)
                    && args(comTaskExecution);

    before (JobExecution jobExecution, ComTaskExecution comTaskExecution): comTaskExecutionStarts(jobExecution, comTaskExecution) {
        jobExecution.getExecutionContext().executing.start();
        if (this.isConnected(jobExecution.getExecutionContext())) {
            ComPortRelatedComChannelImpl comChannel = (ComPortRelatedComChannelImpl) jobExecution.getExecutionContext().getComChannel();
            Counters taskSessionCounters = this.getComChannelTaskSessionCounters(comChannel);
            taskSessionCounters.resetBytesRead();
            taskSessionCounters.resetBytesSent();
            taskSessionCounters.resetPacketsRead();
            taskSessionCounters.resetPacketsSent();
        }
    }

    private pointcut comTaskExecutionCompletes (JobExecution scheduledJob, ComTaskExecution comTaskExecution):
            execution(void JobExecution.completeExecutedComTask(ComTaskExecution))
         && target(scheduledJob)
         && args(comTaskExecution);

    after (JobExecution jobExecution, ComTaskExecution comTaskExecution): comTaskExecutionCompletes(jobExecution, comTaskExecution) {
        jobExecution.getExecutionContext().executing.stop();
        this.comTaskExecutionCompleted(jobExecution, comTaskExecution, null);
    }

    private pointcut comTaskExecutionFails (JobExecution scheduledJob, ComTaskExecution comTaskExecution, Throwable t):
            execution(void JobExecution.failure(ComTaskExecution, java.lang.Throwable))
                    && target(scheduledJob)
                    && args(comTaskExecution, t);

    after (JobExecution jobExecution, ComTaskExecution comTaskExecution, Throwable t): comTaskExecutionFails(jobExecution, comTaskExecution, t) {
        jobExecution.getExecutionContext().executing.stop();
        this.comTaskExecutionCompleted(jobExecution, comTaskExecution, t);
    }

    private void comTaskExecutionCompleted (JobExecution jobExecution, ComTaskExecution comTaskExecution, Throwable t) {
        if (this.isConnected(jobExecution.getExecutionContext()) && this.hasCurrentTaskSession(jobExecution)) {
            ComPortRelatedComChannelImpl comChannel = (ComPortRelatedComChannelImpl) jobExecution.getExecutionContext().getComChannel();
            ComStatisticsShadow comStatistics = jobExecution.getExecutionContext().getCurrentTaskExecutionSession().getComStatistics();
            Counters taskSessionCounters = this.getComChannelTaskSessionCounters(comChannel);
            comStatistics.setNrOfBytesSent(taskSessionCounters.getBytesSent());
            comStatistics.setNrOfBytesRead(taskSessionCounters.getBytesRead());
            comStatistics.setNrOfPacketsSent(taskSessionCounters.getPacketsSent());
            comStatistics.setNrOfPacketsRead(taskSessionCounters.getPacketsRead());
        }

    }

    private boolean hasCurrentTaskSession(JobExecution jobExecution){
        return jobExecution.getExecutionContext().getCurrentTaskExecutionSession() != null;
    }

    private pointcut reading (ComPortRelatedComChannelImpl comChannel):
            execution(int ComPortRelatedComChannelImpl+.read(..))
         && target(comChannel);

    before (ComPortRelatedComChannelImpl comChannel): reading(comChannel) {
        StopWatch talking = this.getComChannelTalkCounter(comChannel);
        talking.start();
    }

    after (ComPortRelatedComChannelImpl comChannel) returning (int bytesRead) : reading(comChannel) {
        StopWatch talking = this.getComChannelTalkCounter(comChannel);
        talking.stop();
        Counters sessionCounters = this.getComChannelSessionCounters(comChannel);
        sessionCounters.bytesRead(bytesRead);
        if(!sessionCounters.isReading()){
            sessionCounters.reading();
            sessionCounters.packetRead();
        }
        Counters taskSessionCounters = this.getComChannelTaskSessionCounters(comChannel);
        taskSessionCounters.bytesRead(bytesRead);
        if(!taskSessionCounters.isReading()){
            taskSessionCounters.reading();
            taskSessionCounters.packetRead();
        }
    }

    private pointcut writing (ComPortRelatedComChannelImpl comChannel):
            execution(int ComPortRelatedComChannelImpl+.write(..))
         && target(comChannel);

    before (ComPortRelatedComChannelImpl comChannel): writing(comChannel) {
        StopWatch talking = this.getComChannelTalkCounter(comChannel);
        talking.start();
    }

    after (ComPortRelatedComChannelImpl comChannel) returning (int bytesSent) : writing(comChannel) {
        StopWatch talking = this.getComChannelTalkCounter(comChannel);
        talking.stop();
        Counters sessionCounters = this.getComChannelSessionCounters(comChannel);
        sessionCounters.bytesSent(bytesSent);
        if (!sessionCounters.isWriting()) {
            sessionCounters.writing();
            sessionCounters.packetSent();
        }
        Counters taskSessionCounters = this.getComChannelTaskSessionCounters(comChannel);
        taskSessionCounters.bytesSent(bytesSent);
        if (!taskSessionCounters.isWriting()) {
            taskSessionCounters.writing();
            taskSessionCounters.packetSent();
        }
    }

}