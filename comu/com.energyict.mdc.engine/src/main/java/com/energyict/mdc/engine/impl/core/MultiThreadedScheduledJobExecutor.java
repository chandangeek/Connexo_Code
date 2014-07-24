package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.model.ComServer;
import com.google.common.base.Optional;

import java.util.Locale;
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
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;

    public MultiThreadedScheduledJobExecutor(TransactionService transactionExecutor, ComServer.LogLevel logLevel, BlockingQueue<ScheduledJob> jobBlockingQueue, DeviceCommandExecutor deviceCommandExecutor, ThreadPrincipalService threadPrincipalService, UserService userService) {
        super(transactionExecutor, logLevel, deviceCommandExecutor);
        this.jobBlockingQueue = jobBlockingQueue;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    @Override
    public void run () {
        Optional<User> user = userService.findUser("batch executor");
        if (user.isPresent()) {
            threadPrincipalService.set(user.get(), "MultiThreadedComPort", "Executing", Locale.ENGLISH);
        }
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
