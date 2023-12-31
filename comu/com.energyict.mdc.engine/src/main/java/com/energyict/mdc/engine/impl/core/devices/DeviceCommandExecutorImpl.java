/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.devices;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.FreeUnusedTokenDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.NoResourcesAcquiredException;
import com.energyict.mdc.engine.impl.concurrent.ResizeableSemaphore;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.DeviceCommandExecutorLogHandler;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides a default implementation for the {@link DeviceCommandExecutor} interface
 * that uses the EIServer persistence framework for all requests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (09:24)
 */
public class DeviceCommandExecutorImpl implements DeviceCommandExecutor, DeviceCommandExecutorConfigurationChangeListener {

    private static final int FULL_LOAD_PERCENTAGE = 100;
    private final PriorityConfigurableThreadFactory threadFactory;
    private final Clock clock;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventPublisher eventPublisher;
    private final WorkQueue workQueue;
    private final ComServer.LogLevel logLevel;
    private final DeviceMessageService deviceMessageService;
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private int numberOfThreads;
    private ExecutorService executorService;
    private ComServerDAO comServerDAO;
    private String name;
    private CompositeDeviceCommandExecutorLogger logger;

    private static final Logger LOGGER = Logger.getLogger(DeviceCommandExecutorImpl.class.getName());

    public DeviceCommandExecutorImpl(String comServerName, int queueCapacity, int numberOfThreads, int threadPriority, ComServer.LogLevel logLevel, ThreadFactory threadFactory, Clock clock, ComServerDAO comServerDAO, EventPublisher eventPublisher, ThreadPrincipalService threadPrincipalService, DeviceMessageService deviceMessageService) {
        super();
        this.clock = clock;
        this.eventPublisher = eventPublisher;
        this.workQueue = new WorkQueue(queueCapacity);
        this.name = "Device command executor for " + comServerName;
        this.threadFactory = new PriorityConfigurableThreadFactory(threadFactory, threadPriority, name);
        this.numberOfThreads = numberOfThreads;
        this.logLevel = logLevel;
        this.comServerDAO = comServerDAO;
        this.threadPrincipalService = threadPrincipalService;
        this.deviceMessageService = deviceMessageService;
        this.initializeLogging();
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

    private void initializeLogging() {
        this.logger = new CompositeDeviceCommandExecutorLogger(this.newNormalLogger(), this.newEventLogger());
    }

    private DeviceCommandExecutorLogger newNormalLogger() {
        return LoggerFactory.getLoggerFor(DeviceCommandExecutorLogger.class, this.toLogLevel(this.getLogLevel()));
    }

    private DeviceCommandExecutorLogger newEventLogger() {
        return LoggerFactory.getLoggerFor(DeviceCommandExecutorLogger.class, this.getAnonymousLogger());
    }

    private Logger getAnonymousLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new DeviceCommandExecutorLogHandler(this.eventPublisher, new ComServerEventServiceProvider()));
        return logger;
    }

    private LogLevel toLogLevel(ComServer.LogLevel logLevel) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(logLevel);
    }

    private void logCurrentQueueSize() {
        this.logger.logCurrentQueueSize(this.getCurrentSize(), this.getCapacity());
    }

    private void startExecutorService() {
        this.executorService = new ExtendedThreadPoolExecutor(
                this.numberOfThreads, this.numberOfThreads, // Fixed size
                0L, TimeUnit.MILLISECONDS,                  // No need to terminate threads when fixed size
                new PriorityBlockingQueue<>(                // Prioritize Workers as they are submitted
                        this.getCapacity(),
                        new WorkerComparator()),
                this.threadFactory);
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
        this.logger.shuttingDown(this);
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.shutdownExecutorService(immediate);
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void shutdownExecutorService(boolean immediate) {
        if (immediate) {
            List<Runnable> waiting = this.executorService.shutdownNow();
            for (Runnable runnable : waiting) {
                Worker waitingWorker = (Worker) ((ExtendedFutureTask) runnable).getCallable();
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

    @SuppressWarnings("unchecked")
    @Override
    public synchronized List<DeviceCommandExecutionToken> tryAcquireTokens(int numberOfCommands) {
        if (this.isRunning()) {
            List<? extends DeviceCommandExecutionToken> acquiredTokens = this.workQueue.tryAcquire(numberOfCommands);
            this.logTryAcquiredTokensResult(numberOfCommands, acquiredTokens);
            return (List<DeviceCommandExecutionToken>) acquiredTokens;
        } else {
            IllegalStateException illegalStateException = this.shouldBeRunning();
            this.logger.cannotPrepareWhenNotRunning(illegalStateException, this);
            throw illegalStateException;
        }
    }

    private void logTryAcquiredTokensResult(int numberOfCommands, List<? extends DeviceCommandExecutionToken> acquiredTokens) {
        if (acquiredTokens.isEmpty()) {
            this.logger.preparationFailed(this, numberOfCommands);
        } else {
            this.logger.preparationCompleted(this, numberOfCommands);
        }
        this.logCurrentQueueSize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DeviceCommandExecutionToken> acquireTokens(int numberOfCommands) throws InterruptedException {
        if (this.isRunning()) {
            this.logCurrentQueueSize();
            if (this.getCurrentSize() == this.getCapacity()) {
                this.logger.preparationFailed(this, numberOfCommands);
            }
            return (List<DeviceCommandExecutionToken>) this.workQueue.acquire(numberOfCommands);
        } else {
            IllegalStateException illegalStateException = this.shouldBeRunning();
            this.logger.cannotPrepareWhenNotRunning(illegalStateException, this);
            throw illegalStateException;
        }
    }

    private boolean isRunning() {
        return ServerProcessStatus.STARTED.equals(this.status);
    }

    private IllegalStateException shouldBeRunning() {
        return new IllegalStateException(MessageFormat.format("DeviceCommandExecutor should be running but current state is {0}", this.status));
    }

    @Override
    public Future<Boolean> execute(DeviceCommand command, DeviceCommandExecutionToken token) {
        if (this.isRunning()) {
            Future<Boolean> future = this.doExecute(command, token);
            this.logger.executionQueued(this, command);
            return future;
        } else {
            IllegalStateException illegalStateException = this.shouldBeRunning();
            this.logger.cannotExecuteWhenNotRunning(illegalStateException, this, command);
            throw illegalStateException;
        }
    }

    private synchronized Future<Boolean> doExecute(DeviceCommand command, DeviceCommandExecutionToken token) {
        this.workQueue.execute(command, token);
        LOGGER.info("CXO-11731: Submit command"+command);
        return this.executorService.submit(new Worker(command, this.comServerDAO));
    }

    @Override
    public void free(DeviceCommandExecutionToken unusedToken) {
        this.doExecute(new FreeUnusedTokenDeviceCommand(), unusedToken);
    }

    @Override
    public synchronized void freeSilently(DeviceCommandExecutionToken unusedToken) {
        this.workQueue.executeIfExpectedToken(new FreeUnusedTokenDeviceCommand(), unusedToken)
                .map(command -> new Worker(command, this.comServerDAO))
                .ifPresent(this.executorService::submit);
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
        if (command instanceof FreeUnusedTokenDeviceCommand) {
            this.logger.tokenReleased(this);
        } else {
            this.logger.commandCompleted(this, command);
        }
        this.workQueue.commandCompleted(command);
        this.logCurrentQueueSize();
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
        this.logger.commandFailed(t, this, command);
        this.logCurrentQueueSize();
    }

    @Override
    public int getThreadPriority() {
        return this.threadFactory.getPriority();
    }

    @Override
    public String getAcquiredTokenThreadNames() {
        return this.workQueue.getAcquiredTokenThreadNames();
    }

    @Override
    public void changeThreadPriority(int newPriority) {
        if (this.getThreadPriority() != newPriority) {
            this.logger.threadPriorityChanged(this, this.getThreadPriority(), newPriority);
        }
        this.threadFactory.setPriority(newPriority);
    }

    private int getQueueCapacity() {
        return this.workQueue.getCapacity();
    }

    @Override
    public synchronized void changeQueueCapacity(int newCapacity) {
        if (this.getQueueCapacity() != newCapacity) {
            this.logger.queueCapacityChanged(this, this.getQueueCapacity(), newCapacity);
        }
        this.workQueue.changeCapacity(newCapacity);
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public synchronized void changeNumberOfThreads(int newNumberOfThreads) {
        if (this.numberOfThreads != newNumberOfThreads) {
            this.logger.numberOfThreadsChanged(this, this.numberOfThreads, newNumberOfThreads);
            this.numberOfThreads = newNumberOfThreads;
            ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executorService;
            executor.setCorePoolSize(newNumberOfThreads);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    private class WorkerComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable o1, Runnable o2) {
            return this.doCompare((Worker) ((ExtendedFutureTask) o1).getCallable(), (Worker) ((ExtendedFutureTask) o2).getCallable());
        }

        private int doCompare(Worker o1, Worker o2) {
            if (o1.command instanceof FreeUnusedTokenDeviceCommand) {
                if (o2.command instanceof FreeUnusedTokenDeviceCommand) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                if (o2.command instanceof FreeUnusedTokenDeviceCommand) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * Extension of the normal ThreadPoolExecutor, wrapping any submitted Callables into ExtendedFutureTask instead of normal FutureTask.
     */
    private class ExtendedThreadPoolExecutor extends ThreadPoolExecutor {

        ExtendedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            return new ExtendedFutureTask<>(callable);
        }
    }

    /**
     * Extension of the normal FutureTask, providing an extra getter for the original Callable
     */
    private class ExtendedFutureTask<V> extends FutureTask<V> {

        private Callable callable = null;

        ExtendedFutureTask(Callable<V> callable) {
            super(callable);
            this.callable = callable;
        }

        Callable getCallable() {
            return callable;
        }
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
    private final class Worker implements Callable<Boolean> {
        private DeviceCommand command;
        private ComServerDAO comServerDAO;

        private Worker(DeviceCommand command, ComServerDAO comServerDAO) {
            super();
            this.command = command;
            this.comServerDAO = comServerDAO;
        }

        /**
         * Need to do this so the Kore knows who did what in the database.
         */
        private void assignThreadUser() {
            User comServerUser = comServerDAO.getComServerUser();
            threadPrincipalService.set(comServerUser, "ComServer", "Store", comServerUser.getLocale().orElse(Locale.ENGLISH));
        }

        @Override
        public Boolean call() {
            return this.doCall(false);
        }

        Boolean runDuringShutdown() {
            return this.doCall(true);
        }

        /**
         * Execute the command, return true if it succeeded, or false if it failed for any reason.
         */
        private Boolean doCall(boolean duringShutdown) {
            Throwable causeOfFailure = null;
            try {
                this.assignThreadUser();
                return comServerDAO.executeTransaction(() -> {
                    if (duringShutdown) {
                        this.command.executeDuringShutdown(this.comServerDAO);
                    } else {
                        LOGGER.info("CXO-11731: Execute command"+this.command);
                        this.command.execute(this.comServerDAO);
                    }
                    return Boolean.TRUE;
                });
            } catch (Exception t) {
                causeOfFailure = t;
                return Boolean.FALSE;
            } finally {
                // in both cases the semaphore is released
                if (causeOfFailure == null) {
                    workerCompleted(this);
                } else {
                    workerFailed(this, causeOfFailure);
                }
                threadPrincipalService.clear();
            }
        }
    }

    private final class DeviceCommandExecutionTokenImpl implements DeviceCommandExecutionToken {
        private final int id;
        private final String threadName;  // For debugging purposes

        private DeviceCommandExecutionTokenImpl(int id) {
            super();
            this.id = id;
            this.threadName = Thread.currentThread().getName();
        }

        private String getThreadName() {
            return threadName;
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

    private class ThreadNameAndCount {
        private final String threadName;
        private int count = 1;

        private ThreadNameAndCount(String threadName) {
            this.threadName = threadName;
        }

        private ThreadNameAndCount increment() {
            ThreadNameAndCount incremented = new ThreadNameAndCount(this.threadName);
            incremented.count = this.count + 1;
            return incremented;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.threadName);
            if (this.count > 1) {
                builder
                        .append(" (")
                        .append(this.count)
                        .append(")");
            }
            return builder.toString();
        }
    }

    private class ThreadNames {
        private Map<String, ThreadNameAndCount> threadNames = new HashMap<>();

        private void add(String threadName) {
            this.threadNames.compute(
                    threadName,
                    (k, v) -> v == null ? new ThreadNameAndCount(k) : v.increment());
        }

        public String toString() {
            return this.threadNames
                    .values()
                    .stream()
                    .map(ThreadNameAndCount::toString)
                    .collect(Collectors.joining(", "));
        }
    }

    private final class TokenList {
        private AtomicInteger nextTokenId = new AtomicInteger(1);
        private Set<DeviceCommandExecutionTokenImpl> tokens;

        private TokenList() {
            super();
            this.tokens = new ConcurrentHashSet<>();
        }

        public boolean remove(DeviceCommandExecutionToken token) {
            return this.tokens.remove(token);
        }

        List<? extends DeviceCommandExecutionToken> addNew(int numberOfTokens) {
            List<DeviceCommandExecutionTokenImpl> newTokens = new ArrayList<>(numberOfTokens);
            for (int i = 0; i < numberOfTokens; i++) {
                int id = this.nextTokenId.incrementAndGet();
                newTokens.add(new DeviceCommandExecutionTokenImpl(id));
            }
            this.tokens.addAll(newTokens);
            return newTokens;
        }

        private int size() {
            return this.tokens.size();
        }

        private String getAcquiredTokenThreadNames() {
            ThreadNames threadNames = new ThreadNames();
            List<DeviceCommandExecutionTokenImpl> tokens = new ArrayList<>(this.tokens);
            tokens
                    .stream()
                    .map(DeviceCommandExecutionTokenImpl::getThreadName)
                    .forEach(threadNames::add);
            return threadNames.toString();
        }
    }

    private final class WorkQueue {
        private final TokenList expected;
        private final ResizeableSemaphore semaphore;
        private int capacity;

        private WorkQueue(int capacity) {
            super();
            this.capacity = capacity;
            this.expected = new TokenList();
            this.semaphore = new ResizeableSemaphore(capacity, true);
        }

        private String getAcquiredTokenThreadNames() {
            return this.expected.getAcquiredTokenThreadNames();
        }

        List<? extends DeviceCommandExecutionToken> tryAcquire(int numberOfCommands) {
            if (this.canAcceptWork(numberOfCommands)) {
                return this.expected.addNew(numberOfCommands);
            } else {
                return new ArrayList<>(0);
            }
        }

        List<? extends DeviceCommandExecutionToken> acquire(int numberOfCommands) throws InterruptedException {
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
        void commandCompleted(DeviceCommand command) {
            this.semaphore.release();
        }

        /**
         * Notification that the execution of the {@link DeviceCommand} failed.
         *
         * @param command The DeviceCommand
         */
        void commandFailed(DeviceCommand command) {
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

        private Optional<DeviceCommand> executeIfExpectedToken(DeviceCommand command, DeviceCommandExecutionToken token) {
            return Optional.ofNullable(command).filter(c -> this.expected.remove(token));
        }

        public int getCapacity() {
            return capacity;
        }

        public int getSize() {
            return this.capacity - this.semaphore.availablePermits();
        }

        void changeCapacity(int newCapacity) {
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

    /**
     * Provides an implementation for the {@link DeviceCommandExecutorLogger} interface
     * that acts as a composite for a collection of loggers
     * that delegates all calls to each separate logger.
     */
    private class CompositeDeviceCommandExecutorLogger implements DeviceCommandExecutorLogger {
        private List<DeviceCommandExecutorLogger> loggers;

        private CompositeDeviceCommandExecutorLogger(DeviceCommandExecutorLogger... loggers) {
            super();
            this.loggers = Arrays.asList(loggers);
        }

        @Override
        public void started(DeviceCommandExecutor deviceCommandExecutor) {
            this.loggers.forEach(each -> each.started(deviceCommandExecutor));
        }

        @Override
        public void shuttingDown(DeviceCommandExecutor deviceCommandExecutor) {
            this.loggers.forEach(each -> each.shuttingDown(deviceCommandExecutor));
        }

        @Override
        public void preparationCompleted(DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands) {
            this.loggers.forEach(each -> each.preparationCompleted(deviceCommandExecutor, numberOfCommands));
        }

        @Override
        public void preparationFailed(DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands) {
            this.loggers.forEach(each -> each.preparationFailed(deviceCommandExecutor, numberOfCommands));
        }

        @Override
        public void cannotPrepareWhenNotRunning(IllegalStateException e, DeviceCommandExecutor deviceCommandExecutor) {
            this.loggers.forEach(each -> each.cannotPrepareWhenNotRunning(e, deviceCommandExecutor));
        }

        @Override
        public void executionQueued(DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand) {
            this.loggers.forEach(each -> each.executionQueued(deviceCommandExecutor, deviceCommand));
        }

        @Override
        public void cannotExecuteWhenNotRunning(IllegalStateException e, DeviceCommandExecutor deviceCommandExecutor, DeviceCommand command) {
            this.loggers.forEach(each -> each.cannotExecuteWhenNotRunning(e, deviceCommandExecutor, command));
        }

        @Override
        public void commandCompleted(DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand) {
            this.loggers.forEach(each -> each.commandCompleted(deviceCommandExecutor, deviceCommand));
        }

        @Override
        public void commandFailed(Throwable t, DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand) {
            this.loggers.forEach(each -> each.commandFailed(t, deviceCommandExecutor, deviceCommand));
        }

        @Override
        public void tokenReleased(DeviceCommandExecutor deviceCommandExecutor) {
            this.loggers.forEach(each -> each.tokenReleased(deviceCommandExecutor));
        }

        @Override
        public void logCurrentQueueSize(int queueSize, int capacity) {
            this.loggers.forEach(each -> each.logCurrentQueueSize(queueSize, capacity));
        }

        @Override
        public void threadPriorityChanged(DeviceCommandExecutor deviceCommandExecutor, int oldPriority, int newPriority) {
            this.loggers.forEach(each -> each.threadPriorityChanged(deviceCommandExecutor, oldPriority, newPriority));
        }

        @Override
        public void numberOfThreadsChanged(DeviceCommandExecutor deviceCommandExecutor, int oldNumberOfThreads, int newNumberOfThreads) {
            this.loggers.forEach(each -> each.numberOfThreadsChanged(deviceCommandExecutor, oldNumberOfThreads, newNumberOfThreads));
        }

        @Override
        public void queueCapacityChanged(DeviceCommandExecutor deviceCommandExecutor, int oldCapacity, int newCapacity) {
            this.loggers.forEach(each -> each.queueCapacityChanged(deviceCommandExecutor, oldCapacity, newCapacity));
        }

    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return deviceMessageService;
        }
    }
}