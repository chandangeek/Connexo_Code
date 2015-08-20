package com.energyict.mdc.engine.impl.core.devices;


import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Defines all the log messages for the {@link DeviceCommandExecutor}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-27 (09:05)
 */
public interface DeviceCommandExecutorLogger {

    /**
     * Logs that the specified {@link DeviceCommandExecutor} has started.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     */
    @Configuration(format = "Started device command executor {0} ...", logLevel = LogLevel.INFO)
    public void started (DeviceCommandExecutor deviceCommandExecutor);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} has started the shutdown proces.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     */
    @Configuration(format = "Shutting down device command executor {0} ...", logLevel = LogLevel.INFO)
    public void shuttingDown (DeviceCommandExecutor deviceCommandExecutor);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} successfully prepared
     * the execution of the specified number of DeviceCommands.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param numberOfCommands The number of device commands that were prepared
     */
    @Configuration(format = "{0} successfully prepared the execution of {1} device command(s)", logLevel = LogLevel.DEBUG)
    public void preparationCompleted (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} failed to prepare
     * the execution of the specified number of DeviceCommands.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param numberOfCommands The number of device commands that were prepared
     */
    @Configuration(format = "{0} failed to prepare the execution of {1} device command(s) because the queue is full", logLevel = LogLevel.ERROR)
    public void preparationFailed (DeviceCommandExecutor deviceCommandExecutor, int numberOfCommands);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} failed to prepare
     * the execution of the specified number of DeviceCommands because it was not running.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     */
    @Configuration(format = "{0} is not running and can therefore not prepare the execution of device commands", logLevel = LogLevel.ERROR)
    public void cannotPrepareWhenNotRunning (IllegalStateException e, DeviceCommandExecutor deviceCommandExecutor);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} queued the
     * execution of the specified DeviceCommand.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param deviceCommand The DeviceCommand
     */
    @Configuration(format = "{0} queued the execution of device command(s) {1}", logLevel = LogLevel.DEBUG)
    public void executionQueued (DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} failed to execute
     * DeviceCommands because it was not running.
     *
     * @param e The IllegalStateException that was reported by the DeviceCommandExecutor
     * @param deviceCommandExecutor The DeviceCommandExecutor
     */
    @Configuration(format = "{0} is not running and can therefore not execute device command(s) {1}", logLevel = LogLevel.ERROR)
    public void cannotExecuteWhenNotRunning (IllegalStateException e, DeviceCommandExecutor deviceCommandExecutor, DeviceCommand command);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} completed
     * the execution of the {@link DeviceCommand} succesfully.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param deviceCommand The DeviceCommand
     */
    @Configuration(format = "{0} completed the execution of device command(s) {1}", logLevel = LogLevel.DEBUG)
    public void commandCompleted (DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand);

    /**
     * Logs that the specified {@link DeviceCommandExecutor} failed to complete
     * the execution of the {@link DeviceCommand} succesfully.
     *
     * @param t The cause of the failure
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param deviceCommand The DeviceCommand
     */
    @Configuration(format = "{0} failed to complete the execution of device command {1}", logLevel = LogLevel.ERROR)
    public void commandFailed (Throwable t, DeviceCommandExecutor deviceCommandExecutor, DeviceCommand deviceCommand);

    /**
     * Logs that the token that was acquired to execute a {@link DeviceCommand}
     * was released to the specified {@link DeviceCommandExecutor} due to a failure
     * during the execution of the DeviceCommand.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     *
     */
    @Configuration(format = "{0} released resources acquired to free unused token", logLevel = LogLevel.DEBUG)
    public void tokenReleased(DeviceCommandExecutor deviceCommandExecutor);

    /**
     * Logs the size of the workerQueue
     */
    @Configuration(format = "Current size of worker queue = {0}, capacity = {1}", logLevel = LogLevel.DEBUG)
    public void logCurrentQueueSize(int queueSize, int capacity);

    /**
     * Logs that the priority of threads that actually execute {@link DeviceCommand}s
     * was changed from the old priority to the new priority.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param newPriority The new priority
     * @param oldPriority The old priority
     */
    @Configuration(format = "The thread priority for {0} changed from {1} to {2}", logLevel = LogLevel.INFO)
    public void threadPriorityChanged (DeviceCommandExecutor deviceCommandExecutor, int oldPriority, int newPriority);

    /**
     * Logs that the number of threads that actually execute {@link DeviceCommand}s
     * was changed from the old value value to the new value.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param newNumberOfThreads The new value
     * @param oldNumberOfThreads The old value
     */
    @Configuration(format = "The number of execution threads for {0} changed from {1} to {2}", logLevel = LogLevel.INFO)
    public void numberOfThreadsChanged (DeviceCommandExecutor deviceCommandExecutor, int oldNumberOfThreads, int newNumberOfThreads);

    /**
     * Logs that the capacity of the queue that hold {@link DeviceCommand}s
     * while awaiting execution was changed.
     *
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param newCapacity The new capacity
     * @param oldCapacity The old capacity
     */
    @Configuration(format = "The queue capacity for {0} changed from {1} to {2}", logLevel = LogLevel.INFO)
    public void queueCapacityChanged (DeviceCommandExecutor deviceCommandExecutor, int oldCapacity, int newCapacity);

}