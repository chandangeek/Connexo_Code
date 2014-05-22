package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.concurrent.ResizeableSemaphore;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactory;
import com.energyict.mdc.engine.impl.core.factories.InboundComPortExecutorFactoryImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.protocols.mdc.channels.VoidComChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Provides an implementation for the {@link ComPortListener} interface
 * for an {@link InboundComPort} that supports multiple connections at a time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:27)
 */
public class MultiThreadedComPortListener extends ComChannelBasedComPortListenerImpl {

    private static final TimeDuration RESOURCE_FREE_TIMEOUT = new TimeDuration(5, TimeDuration.SECONDS);
    private final ServiceProvider serviceProvider;

    private ResourceManager resourceManager;
    private ExecutorService executorService;
    private int numberOfThreads;
    private InboundComPortExecutorFactory inboundComPortExecutorFactory;

    public MultiThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this(comPort, comServerDAO, deviceCommandExecutor, new ComServerThreadFactory(comPort.getComServer()), new InboundComPortExecutorFactoryImpl(), serviceProvider);
    }

    public MultiThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        this(comPort, comServerDAO, deviceCommandExecutor, threadFactory, new InboundComPortExecutorFactoryImpl(), serviceProvider);
    }

    protected MultiThreadedComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, InboundComPortExecutorFactory inboundComPortExecutorFactory, ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, threadFactory, deviceCommandExecutor);
        this.serviceProvider = serviceProvider;
        this.setThreadName("MultiThreaded listener for inbound ComPort " + comPort.getName());
        this.numberOfThreads = getComPort().getNumberOfSimultaneousConnections();
        this.resourceManager = new ResourceManager(getComPort().getNumberOfSimultaneousConnections());
        this.inboundComPortExecutorFactory = inboundComPortExecutorFactory;
    }

    @Override
    protected void doStart() {
        this.startExecutorService();
        super.doStart();
    }

    private void startExecutorService() {
        this.executorService = Executors.newFixedThreadPool(this.numberOfThreads, getThreadFactory());
    }

    @Override
    protected void doShutdown() {
        this.shutdownExecutorService(false);
        super.doShutdown();
    }

    @Override
    public void shutdownImmediate() {
        shutdownExecutorService(true);
        super.shutdownImmediate();
    }

    private void shutdownExecutorService(boolean immediate) {
        if (immediate) {
            this.executorService.shutdownNow();
            // we don't do anything with the inbound call. The device will call back ...
        } else {
            this.executorService.shutdown();
            try {
                this.executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void doRun() {

        if (prepareExecution()) {
            ComChannel comChannel = listen();
            if (!(comChannel instanceof VoidComChannel)) {
                this.executorService.execute(new Worker(this.inboundComPortExecutorFactory.create(getComPort(), getComServerDAO(), getDeviceCommandExecutor(), serviceProvider), comChannel));
            }
        } else {
            waitForFreeResources();
        }

    }

    /**
     * Make descent preparations for this listener to be able to accept calls.
     *
     * @return true of we are able to receive inbound calls, false otherwise
     */
    protected boolean prepareExecution() {
        return this.resourceManager.prepareExecution();
    }

    /**
     * Keep myself <i>busy</i> while waiting for resources to be freed
     */
    private void waitForFreeResources() {
        try {
            Thread.sleep(RESOURCE_FREE_TIMEOUT.getMilliSeconds());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void applyChangesForNewComPort(InboundComPort inboundComPort) {
        this.resourceManager.changeCapacity(inboundComPort.getNumberOfSimultaneousConnections());
        ThreadPoolExecutor executor = (ThreadPoolExecutor) this.executorService;
        executor.setCorePoolSize(inboundComPort.getNumberOfSimultaneousConnections());
    }

    protected ResourceManager getResourceManager() {
        return resourceManager;
    }

    protected ExecutorService getExecutorService(){
        return this.executorService;
    }

    /**
     * Does the actual work of executing a {@link InboundComPortExecutor}.
     * A Worker is only created or activated
     * when a InboundComPortExecutor is ready to be executed.
     */
    private final class Worker implements Runnable {

        private final ComChannel comChannel;
        private InboundComPortExecutor inboundComPortExecutor;

        private Worker(InboundComPortExecutor inboundComPortExecutor, ComChannel comChannel) {
            super();
            this.inboundComPortExecutor = inboundComPortExecutor;
            this.comChannel = comChannel;
        }

        @Override
        public void run() {
            this.doRun();
        }

        private void doRun() {
            Throwable causeOfFailure = null;
            try {
                this.inboundComPortExecutor.execute(this.comChannel);
            } catch (Throwable t) {
                /* Use Throwable rather than Exception or BusinessException and SQLException
                 * to make sure that the Semaphore#release method is called
                 * even in the worst of situations. */
                causeOfFailure = t;
            } finally {
                this.comChannel.close();
                if (causeOfFailure == null) {
                    workerCompleted();
                } else {
                    workerFailed(causeOfFailure);
                }
            }
        }
    }

    /**
     * Notification sent by Worker that the execution
     * of a {@link InboundComPortExecutor} completed.
     */
    protected synchronized void workerCompleted() {
        this.commandCompleted();
    }

    private void commandCompleted() {
        this.resourceManager.executionCompleted();
    }


    /**
     * Notification sent by Worker that the execution
     * of a {@link InboundComPortExecutor} failed.
     *
     * @param t The Throwable that caused the failure
     */
    protected synchronized void workerFailed(Throwable t) {
        this.commandFailed(t);
    }

    private void commandFailed(Throwable t) {
        this.resourceManager.executionFailed();
    }

    /**
     * Manages the capacity of this multiThreaded object.
     */
    protected class ResourceManager {

        private int capacity;
        private final ResizeableSemaphore semaphore;

        public ResourceManager(int capacity) {
            super();
            this.capacity = capacity;
            this.semaphore = new ResizeableSemaphore(capacity, true);
        }

        public boolean prepareExecution() {
            return this.semaphore.tryAcquire();
        }

        /**
         * Notification that the execution of an {@link InboundComPortExecutor} completed.
         */
        public void executionCompleted() {
            this.semaphore.release();
        }

        /**
         * Notification that the execution of an {@link InboundComPortExecutor} failed.
         */
        public void executionFailed() {
            this.semaphore.release();
        }

        public int getCapacity() {
            return capacity;
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