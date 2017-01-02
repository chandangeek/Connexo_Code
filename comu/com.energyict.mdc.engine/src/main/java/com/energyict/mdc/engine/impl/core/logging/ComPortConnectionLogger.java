package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.ConnectionException;

/**
 * Defines all the log messages for connection related {@link com.energyict.mdc.engine.config.ComPort} operations.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:34)
 */
public interface ComPortConnectionLogger {

    /**
     * Gets the name of the name of the logging category of the underlying logging framework.
     *
     * @return The log category name
     */
    String getLoggingCategoryName();

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort} is about to start
     * the execution of the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param comPortThreadName The name of the ComPort thread that is starting the execution
     * @param comTaskName       The name of the ComTask whose execution is about to start
     */
    @Configuration(format = "''{0}'' is starting the execution of task ''{1}''", logLevel = LogLevel.DEBUG)
    void startingTask(String comPortThreadName, String comTaskName);

    /**
     * Logs that a ScheduledJobImpl thread has established a connection
     * to a device through the {@link com.energyict.mdc.engine.config.ComPort}.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     */
    @Configuration(format = "''{0}'' established a connection through ComPort ''{1}''!", logLevel = LogLevel.DEBUG)
    void connectionEstablished(String comPortThreadName, String comPortName);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort} failed to establish a connection.
     *
     * @param comPortThreadName The name of the ComPort thread that attempted to start the execution
     */
    @Configuration(format = "''{0}'' failed to establish a connection!", logLevel = LogLevel.ERROR)
    void cannotEstablishConnection(ConnectionException e, String comPortThreadName);

    /**
     * Logs that the specified {@link com.energyict.mdc.engine.config.ComPort} just completing
     * the execution of the {@link com.energyict.mdc.tasks.ComTask}.
     *
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the ComTask
     */
    @Configuration(format = "''{0}'' is completing the execution of task ''{1}''", logLevel = LogLevel.DEBUG)
    void completingTask(String comPortThreadName, String comTaskName);


    /**
     * Logs that the execution of a {@link com.energyict.mdc.tasks.ComTask}
     * by a {@link com.energyict.mdc.engine.config.ComPort} failed due to {@link com.energyict.mdc.upl.issue.Problem}s
     * that were reported while executing.
     *
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the {@link com.energyict.mdc.tasks.ComTask} whose execution failed
     */
    @Configuration(format = "The execution of task ''{1}'' for device ''{2}'' by ''{0}'' failed, see related reported problems", logLevel = LogLevel.ERROR)
    void taskExecutionFailedDueToProblems(String comPortThreadName, String comTaskName, String device);


    /**
     * Logs that the execution of a {@link com.energyict.mdc.tasks.ComTask}
     * by a {@link com.energyict.mdc.engine.config.ComPort} failed due to {@link com.energyict.mdc.upl.issue.Problem}s
     * that were reported while executing.
     *
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the {@link com.energyict.mdc.tasks.ComTask} whose execution failed
     */
    @Configuration(format = "The execution of task ''{1}'' by ''{0}'' failed, see related reported problems", logLevel = LogLevel.ERROR)
    void taskExecutionFailedDueToProblems(String comPortThreadName, String comTaskName);

    /**
     * Logs that the execution of a {@link com.energyict.mdc.tasks.ComTask}
     * by a {@link com.energyict.mdc.engine.config.ComPort} failed with the specified RuntimeException.
     *
     * @param e                 The Throwable that occurred while executing the ComTaskExecution
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the {@link com.energyict.mdc.tasks.ComTask} whose execution failed
     */
    @Configuration(format = "The execution of task ''{1}'' for device ''{2}'' by ''{0}'' failed, see stacktrace below", logLevel = LogLevel.ERROR)
    void taskExecutionFailed(Throwable e, String comPortThreadName, String comTaskName, String device);


    /**
     * Logs that the execution of a {@link com.energyict.mdc.tasks.ComTask}
     * by a {@link com.energyict.mdc.engine.config.ComPort} failed with the specified RuntimeException.
     *
     * @param e                 The RuntimeException that occurred while executing the ComTaskExecution
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the {@link com.energyict.mdc.tasks.ComTask} whose execution failed
     */
    @Configuration(format = "The execution of task ''{1}'' by ''{0}'' failed, see stacktrace below", logLevel = LogLevel.ERROR)
    void taskExecutionFailed(Throwable e, String comPortThreadName, String comTaskName);

    /**
     * Logs that the execution of a {@link com.energyict.mdc.tasks.ComTask} by a
     * {@link com.energyict.mdc.engine.config.ComPort} will be rescheduled due to a previous failure.
     *
     * @param comPortThreadName The name of the ComPort thread that completed the execution
     * @param comTaskName       The name of the ComTask
     */
    @Configuration(format = "The task ''{1}'' executed by ''{0}'' failed and will be rescheduled", logLevel = LogLevel.WARN)
    void reschedulingTask(String comPortThreadName, String comTaskName);

}