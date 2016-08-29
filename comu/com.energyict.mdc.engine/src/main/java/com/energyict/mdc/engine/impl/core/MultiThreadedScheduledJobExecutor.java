package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JobExecutor that takes jobs from a blocking queue.
 * If not jobs are available, then the Thread will wait.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/17/13
 * Time: 11:12 AM
 */
class MultiThreadedScheduledJobExecutor extends ScheduledJobExecutor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadedScheduledJobExecutor.class.getName());

    private final OutboundComPort comPort;
    private BlockingQueue<ScheduledJob> jobBlockingQueue;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;

    MultiThreadedScheduledJobExecutor(OutboundComPort comPort, BlockingQueue<ScheduledJob> jobBlockingQueue, DeviceCommandExecutor deviceCommandExecutor, TransactionService transactionExecutor, ThreadPrincipalService threadPrincipalService, UserService userService) {
        super(transactionExecutor, comPort.getComServer().getCommunicationLogLevel(), deviceCommandExecutor);
        this.comPort = comPort;
        this.jobBlockingQueue = jobBlockingQueue;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    @Override
    public void run() {
        this.setThreadPrinciple();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ScheduledJob scheduledJob = jobBlockingQueue.take();
                acquireTokenAndPerformSingleJob(scheduledJob);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, () -> MultiThreadedScheduledJobExecutor.class.getName() + " for comport(" + this.comPort.getId() + ") encountered and ignored an unexpected problem");
                e.printStackTrace(System.err);
            }
        }
    }

    private void setThreadPrinciple() {
        Optional<User> user = userService.findUser(EngineServiceImpl.COMSERVER_USER);
        user.ifPresent(u -> threadPrincipalService.set(u, "MultiThreadedComPort", "Executing", u.getLocale().orElse(Locale.ENGLISH)));
    }

}