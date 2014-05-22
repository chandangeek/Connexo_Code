package com.energyict.mdc.engine.impl.core.devices;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.FreeUnusedTokenDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.NoResourcesAcquiredException;
import com.energyict.mdc.engine.impl.concurrent.ResizeableSemaphore;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComServerThreadFactory;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.model.ComServer;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a default implementation for the {@link DeviceCommandExecutor} interface
 * that uses the EIServer persistence framework for all requests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (09:24)
 */
public class DeviceCommandExecutorImpl implements DeviceCommandExecutor, DeviceCommandExecutorConfigurationChangeListener {

    private static final int FULL_LOAD_PERCENTAGE = 100;

    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private final PriorityConfigurableThreadFactory threadFactory;
    private final UserService userService;
    private final ThreadPrincipalService threadPrincipalService;
    private int numberOfThreads;
    private final WorkQueue workQueue;
    private ExecutorService executorService;
    private final ComServer.LogLevel logLevel;
    private ComServerDAO comServerDAO;
    private String name;

    public DeviceCommandExecutorImpl(ComServer comServer, int queueCapacity, int numberOfThreads, int threadPriority, ComServer.LogLevel logLevel, ComServerDAO comServerDAO, ThreadPrincipalService threadPrincipalService, UserService userService) {
        this(comServer, queueCapacity, numberOfThreads, threadPriority, logLevel, new ComServerThreadFactory(comServer), comServerDAO, threadPrincipalService, userService);
    }

    public DeviceCommandExecutorImpl(ComServer comServer, int queueCapacity, int numberOfThreads, int threadPriority, ComServer.LogLevel logLevel, ThreadFactory threadFactory, ComServerDAO comServerDAO, ThreadPrincipalService threadPrincipalService, UserService userService) {
        super();
        this.workQueue = new WorkQueue(queueCapacity);
        this.threadFactory = new PriorityConfigurableThreadFactory(threadFactory, threadPriority, name);
        this.numberOfThreads = numberOfThreads;
        this.logLevel = logLevel;
        this.comServerDAO = comServerDAO;
        this.name = "Device command executor for " + comServer.getName();
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    @Override
    public ComServer.LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void start() {
        this.status = ServerProcessStatus.STARTING;
        this.startExecutorService();
        this.status = ServerProcessStatus.STARTED;
    }

    private void startExecutorService() {
        this.executorService = Executors.newFixedThreadPool(this.numberOfThreads, this.threadFactory);
    }

    @Override
    public void shutdown() {
        this.shutdown(false);
    }

    @Override
    public void shutdownImmediate() {
        this.shutdown(true);
    }

    private void shutdown(boolean immediate) {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.shutdownExecutorService(immediate);
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void shutdownExecutorService(boolean immediate) {
        if (immediate) {
            List<Runnable> waiting = this.executorService.shutdownNow();
            for (Runnable runnable : waiting) {
                Worker waitingWorker = (Worker) runnable;
                waitingWorker.runDuringShutdown();
            }
        } else {
            this.executorService.shutdown();
            try {
                this.executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public synchronized List<DeviceCommandExecutionToken> tryAcquireTokens(int numberOfCommands) {
        if (this.isRunning()) {
            return this.workQueue.tryAcquire(numberOfCommands);
        } else {
            throw this.shouldBeRunning();
        }
    }

    @Override
    public List<DeviceCommandExecutionToken> acquireTokens(int numberOfCommands) throws InterruptedException {
        if (this.isRunning()) {
            return this.workQueue.acquire(numberOfCommands);
        } else {
            throw this.shouldBeRunning();
        }
    }

    private boolean isRunning() {
        return ServerProcessStatus.STARTED.equals(this.status);
    }

    private IllegalStateException shouldBeRunning() {
        return new IllegalStateException(MessageFormat.format("DeviceCommandExecutor should be running but current state is {0}", this.status));
    }

    @Override
    public void execute(DeviceCommand command, DeviceCommandExecutionToken token) {
        if (this.isRunning()) {
            this.doExecute(command, token);
        } else {
            throw this.shouldBeRunning();
        }
    }

    private synchronized void doExecute(DeviceCommand command, DeviceCommandExecutionToken token) {
        this.workQueue.execute(command, token);
        this.executorService.execute(new Worker(command, this.comServerDAO));
    }

    @Override
    public void free(DeviceCommandExecutionToken unusedToken) {
        this.doExecute(new FreeUnusedTokenDeviceCommand(), unusedToken);
    }

    @Override
    public int getCapacity() {
        return this.workQueue.getCapacity();
    }

    @Override
    public int getCurrentSize() {
        return this.workQueue.getSize();
    }

    @Override
    public int getCurrentLoadPercentage() {
        int capacity = this.getCapacity();
        if (capacity == 0) {
            return 0;
        } else {
            return (FULL_LOAD_PERCENTAGE * this.getCurrentSize()) / capacity;
        }
    }

    /**
     * Notification sent by Worker that the execution
     * of a {@link DeviceCommand} completed.
     *
     * @param worker The Worker that failed
     */
    private synchronized void workerCompleted(Worker worker) {
        this.commandCompleted(worker.command);
    }

    private void commandCompleted(DeviceCommand command) {
        this.workQueue.commandCompleted(command);
    }

    /**
     * Notification sent by Worker that the execution
     * of a {@link DeviceCommand} failed.
     *
     * @param worker The Worker that failed
     * @param t      The Throwable that caused the failure
     */
    private synchronized void workerFailed(Worker worker, Throwable t) {
        this.commandFailed(worker.command, t);
    }

    private void commandFailed(DeviceCommand command, Throwable t) {
        this.workQueue.commandFailed(command);
    }

    public int getThreadPriority() {
        return this.threadFactory.getPriority();
    }

    @Override
    public void changeThreadPriority(int newPriority) {
        this.threadFactory.setPriority(newPriority);
    }

    public int getQueueCapacity() {
        return this.workQueue.getCapacity();
    }

    @Override
    public synchronized void changeQueueCapacity(int newCapacity) {
        this.workQueue.changeCapacity(newCapacity);
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public synchronized void changeNumberOfThreads(int newNumberOfThreads) {
        if (this.numberOfThreads != newNumberOfThreads) {
            this.numberOfThreads = newNumberOfThreads;
            ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executorService;
            executor.setCorePoolSize(newNumberOfThreads);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Provides an implementation for the ThreadFactory
     * interface that allows to configure the priority
     * of the new Threads that are created.
     * Will delegate to an existing ThreadFactory to do the actual work.
     * Note that changing the priority later on will retroactively change
     * the priority of the Threads that were created in the past.
     */
    private final class PriorityConfigurableThreadFactory implements ThreadFactory {
        private ThreadFactory actualFactory;
        private int priority = Thread.NORM_PRIORITY;
        private List<Thread> threads = new ArrayList<>();
        private String name;

        private PriorityConfigurableThreadFactory(ThreadFactory actualFactory, int priority, String name) {
            super();
            this.actualFactory = actualFactory;
            this.priority = priority;
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = this.actualFactory.newThread(r);
            thread.setPriority(priority);
            thread.setName(name + " " + threads.size());
            this.threads.add(thread);
            return thread;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int newPriority) {
            this.priority = newPriority;
            for (Thread thread : this.threads) {
                thread.setPriority(newPriority);
            }
        }

    }

    /**
     * Does the actual work of executing a {@link DeviceCommand}.
     * A Worker is only created or activated
     * when a DeviceCommand is ready to be executed.
     */
    private final class Worker implements Runnable {
        private DeviceCommand command;
        private ComServerDAO comServerDAO;

        private Worker(DeviceCommand command, ComServerDAO comServerDAO) {
            super();
            this.command = command;
            this.comServerDAO = comServerDAO;
        }

        /**
         * Need to do this so the Kore knows who did what in the database
         */
        private void assignThreadUser() {
            // TODO in the future, a ComServer user will be created, we should use that one if it exists
            Optional<User> user = userService.findUser("batch executor");
            if (user.isPresent()) {
                threadPrincipalService.set(user.get(), "ComServer", "Store", Locale.ENGLISH);
            }
        }

        @Override
        public void run() {
            this.doRun(false);
        }

        public void runDuringShutdown() {
            this.doRun(true);
        }

        private void doRun(boolean duringShutdown) {
            assignThreadUser();
            Throwable causeOfFailure = null;
            try {
                if (duringShutdown) {
                    this.command.executeDuringShutdown(this.comServerDAO);
                } else {
                    this.command.execute(this.comServerDAO);
                }
            } catch (Throwable t) {
                /* Use Throwable rather than Exception or BusinessException and SQLException
                 * to make sure that the Semaphore#release method is called
                 * even in the worst of situations. */
                causeOfFailure = t;
            } finally {
                if (causeOfFailure == null) {
                    workerCompleted(this);
                } else {
                    workerFailed(this, causeOfFailure);
                }
            }
        }
    }

    private final class DeviceCommandExecutionTokenImpl implements DeviceCommandExecutionToken {
        private int id;

        private DeviceCommandExecutionTokenImpl(int id) {
            super();
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DeviceCommandExecutionTokenImpl that = (DeviceCommandExecutionTokenImpl) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

    }

    private final class TokenList {
        private AtomicInteger nextTokenId = new AtomicInteger(1);
        private Set<DeviceCommandExecutionToken> tokens;

        private TokenList() {
            super();
            this.tokens = new ConcurrentHashSet<>();
        }

        public boolean remove(DeviceCommandExecutionToken token) {
            return this.tokens.remove(token);
        }

        public List<DeviceCommandExecutionToken> addNew(int numberOfTokens) {
            List<DeviceCommandExecutionToken> newTokens = new ArrayList<>(numberOfTokens);
            for (int i = 0; i < numberOfTokens; i++) {
                int id = this.nextTokenId.incrementAndGet();
                newTokens.add(new DeviceCommandExecutionTokenImpl(id));
            }
            this.tokens.addAll(newTokens);
            return newTokens;
        }

    }

    private final class WorkQueue {
        private int capacity;
        private final TokenList expected;
        private final ResizeableSemaphore semaphore;

        private WorkQueue(int capacity) {
            super();
            this.capacity = capacity;
            this.expected = new TokenList();
            this.semaphore = new ResizeableSemaphore(capacity, true);
        }

        public List<DeviceCommandExecutionToken> tryAcquire(int numberOfCommands) {
            if (this.canAcceptWork(numberOfCommands)) {
                return this.expected.addNew(numberOfCommands);
            } else {
                return new ArrayList<>(0);
            }
        }

        public List<DeviceCommandExecutionToken> acquire(int numberOfCommands) throws InterruptedException {
            this.semaphore.acquire(numberOfCommands);
            return this.expected.addNew(numberOfCommands);
        }

        private boolean canAcceptWork(int numberOfCommands) {
            return this.semaphore.tryAcquire(numberOfCommands);
        }

        /**
         * Notification that the execution of the {@link DeviceCommand} completed.
         *
         * @param command The DeviceCommand
         */
        public void commandCompleted(DeviceCommand command) {
            this.semaphore.release();
        }

        /**
         * Notification that the execution of the {@link DeviceCommand} failed.
         *
         * @param command The DeviceCommand
         */
        public void commandFailed(DeviceCommand command) {
            this.semaphore.release();
        }

        public DeviceCommand execute(DeviceCommand command, DeviceCommandExecutionToken token) {
            if (this.expected.remove(token)) {
                // The token was indeed expected so return the DeviceCommand to schedule its execution
                return command;
            } else {
                throw new NoResourcesAcquiredException();
            }
        }

        public int getCapacity() {
            return capacity;
        }

        public int getSize() {
            return this.capacity - this.semaphore.availablePermits();
        }

        public void changeCapacity(int newCapacity) {
            if (this.reducingCapacity(newCapacity)) {
                this.semaphore.reducePermits(this.capacity - newCapacity);
            } else if (this.extendingCapacity(newCapacity)) {
                this.semaphore.release(newCapacity - this.capacity);
            }
            this.capacity = newCapacity;
        }

        private boolean reducingCapacity(int newCapacity) {
            return this.capacity > newCapacity;
        }

        private boolean extendingCapacity(int newCapacity) {
            return this.capacity < newCapacity;
        }
    }

}