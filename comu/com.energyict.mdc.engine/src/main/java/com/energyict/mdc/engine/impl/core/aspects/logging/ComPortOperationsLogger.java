package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Defines all the log messages for the operational aspects of a {@link ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:29)
 */
public interface ComPortOperationsLogger {

    /**
     * Gets the name of the name of the logging category of the underlying logging framework.
     *
     * @return The log category name
     */
    public String getLoggingCategoryName ();

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
     * Logs that the specified {@link ComPort} is now monitoring for changes.
     *
     * @param comPort The ComPort
     */
    @Configuration(format = "Checking for changes applied to outbound ComPort ''{0}''...", logLevel = LogLevel.DEBUG)
    public void monitoringChanges (ComPort comPort);

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
     * Logs that the specified {@link ComPort} did not schedule
     * any of the executable work that was found earlier.
     *
     * @param comPortThreadName The name of the ComPort thread that did not schedule the work
     */
    @Configuration(format = "{0} did not schedule any of the work found", logLevel = LogLevel.DEBUG)
    public void noWorkScheduled (String comPortThreadName);

    /**
     * Logs that the specified {@link ComPort} found executable work.
     *
     * @param comPortThreadName The name of the ComPort thread that found work
     * @param numberOfJobs The amount of work that was scheduled
     * @param availableJobs The total amount of work that was available to schedule
     */
    @Configuration(format = "{0} scheduled {1} job(s) of the {2} that were found earlier", logLevel = LogLevel.DEBUG)
    public void workScheduled (String comPortThreadName, int numberOfJobs, int availableJobs);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.model.ComPort} attempted to schedule
     * the execution of the {@link ComTaskExecution} but it was already scheduled.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     *                          and had already scheduled the ComTaskExecution
     * @param comTaskExecution The ComTaskExecution
     */
    @Configuration(format = "{1} is already scheduled for execution by ''{0}''", logLevel = LogLevel.DEBUG)
    public void alreadyScheduled (String comPortThreadName, ComTaskExecution comTaskExecution);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.model.ComPort} attempted to schedule
     * the execution of the {@link ComTaskExecution} but the work queue was full.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     * @param comTaskExecution The ComTaskExecution
     */
    @Configuration(format = "''{0}'' cannot schedule ComTaskExecution {1} for execution because the queue is full!", logLevel = LogLevel.WARN)
    public void cannotSchedule (String comPortThreadName, ComTaskExecution comTaskExecution);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.model.ComPort} unschedule
     * the execution of the {@link ComTaskExecution} because another ComPort
     * has picked up the ComTaskExecution for execution.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     * @param comTaskExecution The ComTaskExecution
     */
    @Configuration(format = "ComTaskExecution {1} is already being executed by another ComPort then ''{0}'' , unscheduling it now.", logLevel = LogLevel.DEBUG)
    public void unscheduled (String comPortThreadName, ComTaskExecution comTaskExecution);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.model.ComPort}
     * ran into an unexpected problem.
     *
     * @param unexpected The unexpected problem
     * @param comPortThreadName The name of the ComPort thread that ran into an unexpected problem
     */
    @Configuration(format = "ComPort ''{0}'' ran into the following unexpected problem:", logLevel = LogLevel.ERROR)
    public void unexpectedError (Throwable unexpected, String comPortThreadName);

}