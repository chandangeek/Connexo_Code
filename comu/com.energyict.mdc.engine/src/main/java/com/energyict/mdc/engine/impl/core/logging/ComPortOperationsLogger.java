package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Defines all the log messages for the operational aspects of a {@link ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:29)
 */
public interface ComPortOperationsLogger {

    /**
     * Logs that an {@link ComPort} has started.
     *
     * @param threadName The name of the thread that started
     */
    @Configuration(format = "Started ''{0}'' ...", logLevel = LogLevel.INFO)
    public void started (String threadName);

    /**
     * Logs that an {@link ComPort} has started the shutdown proces.
     *
     * @param threadName The name of the thread that is shutting down
     */
    @Configuration(format = "Shutting down ''{0}''...", logLevel = LogLevel.INFO)
    public void shuttingDown (String threadName);

    /**
     * Logs that the specified {@link ComPort} is looking for work.
     *
     * @param comPortThreadName The name of the ComPort thread that is looking for work
     */
    @Configuration(format = "{0} is looking for work...", logLevel = LogLevel.DEBUG)
    public void lookingForWork (String comPortThreadName);

    /**
     * Logs that the specified {@link ComPort} did not find any executable work.
     *
     * @param comPortThreadName The name of the ComPort thread that could not find work
     */
    @Configuration(format = "{0} found no work to execute", logLevel = LogLevel.DEBUG)
    public void noWorkFound (String comPortThreadName);

    /**
     * Logs that the specified {@link ComPort} found executable work.
     *
     * @param comPortThreadName The name of the ComPort thread that found work
     * @param numberOfJobs The amount of work that was found
     */
    @Configuration(format = "{0} found {1} job(s) to execute", logLevel = LogLevel.DEBUG)
    public void workFound (String comPortThreadName, int numberOfJobs);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort} attempted to schedule
     * the execution of the {@link ComTaskExecution} but it was already scheduled.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     *                          and had already scheduled the ComTaskExecution
     * @param comTaskExecution The ComTaskExecution
     */
    @Configuration(format = "{1} is already scheduled for execution by ''{0}''", logLevel = LogLevel.DEBUG)
    public void alreadyScheduled (String comPortThreadName, ComTaskExecution comTaskExecution);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort} attempted to schedule
     * the execution of the {@link ComTaskExecution} but the work queue was full.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     * @param comTaskExecution The ComTaskExecution
     */
    @Configuration(format = "''{0}'' cannot schedule ComTaskExecution {1} for execution because the queue is full!", logLevel = LogLevel.WARN)
    public void cannotSchedule (String comPortThreadName, ComTaskExecution comTaskExecution);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort}
     * ran into an unexpected problem.
     *
     * @param comPortThreadName The name of the ComPort thread that ran into an unexpected problem
     * @param unexpected The unexpected problem
     */
    @Configuration(format = "ComPort ''{0}'' ran into the following unexpected problem:", logLevel = LogLevel.ERROR)
    public void unexpectedError(String comPortThreadName, Throwable unexpected);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort}
     * ran into the same unexpected problem and will therefore not log the stacktrace.
     *
     * @param comPortThreadName The name of the ComPort thread that ran into an unexpected problem
     * @param message The message of the unexpected problem
     */
    @Configuration(format = "ComPort ''{0}'' ran into the following unexpected problem: {1}", logLevel = LogLevel.ERROR)
    public void unexpectedError (String comPortThreadName, String message);

}