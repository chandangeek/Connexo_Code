package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;

import java.util.Locale;
import java.util.Optional;
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
public class MultiThreadedScheduledJobExecutor extends ScheduledJobExecutor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(MultiThreadedScheduledJobExecutor.class.getName());
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final ScheduledJob scheduledJob;

    public MultiThreadedScheduledJobExecutor(ScheduledJob scheduledJob, TransactionService transactionExecutor, ComServer.LogLevel communicationLogLevel, DeviceCommandExecutor deviceCommandExecutor, ThreadPrincipalService threadPrincipalService, UserService userService) {
        super(transactionExecutor, communicationLogLevel, deviceCommandExecutor);
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.scheduledJob = scheduledJob;
    }

    @Override
    public void run() {
        this.setThreadPrinciple();
        try {
            acquireTokenAndPerformSingleJob(scheduledJob);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, MultiThreadedScheduledJobExecutor.class.getName() + " encountered and ignored an unexpected problem", t);
            t.printStackTrace(System.err);
        }
    }

    private void setThreadPrinciple() {
        Optional<User> user = userService.findUser(EngineServiceImpl.COMSERVER_USER);
        user.ifPresent(u -> threadPrincipalService.set(u, "MultiThreadedComPort", "Executing", Locale.ENGLISH));
    }
}