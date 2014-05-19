package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;

import java.util.concurrent.BlockingQueue;

/**
 * JobExecutor that takes jobs from a blocking queue.
 * If not jobs are available, then the Thread will wait.
 *
 * Copyrights EnergyICT
 * Date: 9/17/13
 * Time: 11:12 AM
 */
public class MultiThreadedScheduledJobExecutor extends ScheduledJobExecutor implements Runnable {

    private BlockingQueue<ScheduledJob> jobBlockingQueue;

    public MultiThreadedScheduledJobExecutor(TransactionService transactionExecutor, ComServer.LogLevel logLevel, BlockingQueue<ScheduledJob> jobBlockingQueue, DeviceCommandExecutor deviceCommandExecutor) {
        super(transactionExecutor, logLevel, deviceCommandExecutor);
        this.jobBlockingQueue = jobBlockingQueue;
    }

    @Override
    public void run () {
        while (!Thread.currentThread().isInterrupted()){
            try {
                ScheduledJob scheduledJob = jobBlockingQueue.take();
                acquireTokenAndPerformSingleJob(scheduledJob);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
