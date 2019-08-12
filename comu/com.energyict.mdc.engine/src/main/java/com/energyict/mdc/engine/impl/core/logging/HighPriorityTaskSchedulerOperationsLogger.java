/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.core.HighPriorityTaskScheduler;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;

public interface HighPriorityTaskSchedulerOperationsLogger {

    /**
     * Logs that a {@link HighPriorityTaskScheduler} has started.
     *
     * @param threadName The name of the thread that started
     */
    @Configuration(format = "Started ''{0}'' ...", logLevel = LogLevel.INFO)
    void started(String threadName);

    /**
     * Logs that an {@link HighPriorityTaskScheduler} has started the shutdown proces.
     *
     * @param threadName The name of the thread that is shutting down
     */
    @Configuration(format = "Shutting down ''{0}''...", logLevel = LogLevel.INFO)
    void shuttingDown(String threadName);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler} is looking for work.
     *
     * @param threadName The name of the ComPort thread that is looking for work
     */
    @Configuration(format = "{0} is looking for work...", logLevel = LogLevel.DEBUG)
    void lookingForWork(String threadName);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler} is not looking for work
     * because the {@link RunningComServer} cannot accept any high priority tasks at the moment.
     *
     * @param threadName The name of the ComPort thread that is looking for work
     */
    @Configuration(format = "{0} is temporary not looking for work because the comserver does not have any active ports", logLevel = LogLevel.INFO)
    void notLookingForWork(String threadName);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler} did not find any executable work.
     *
     * @param threadNqme The name of the ComPort thread that could not find work
     */
    @Configuration(format = "{0} found no work to execute", logLevel = LogLevel.DEBUG)
    void noWorkFound(String threadNqme);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler} found executable work.
     *
     * @param threadName The name of the ComPort thread that found work
     * @param numberOfJobs The amount of work that was found
     */
    @Configuration(format = "{0} found {1} job(s) to execute", logLevel = LogLevel.DEBUG)
    void workFound(String threadName, int numberOfJobs);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler}
     * ran into an unexpected problem.
     *
     * @param unexpected The unexpected problem
     * @param threadName The name of the ComPort thread that ran into an unexpected problem
     */
    @Configuration(format = "Priority task scheduler ''{0}'' ran into the following unexpected problem:", logLevel = LogLevel.ERROR)
    void unexpectedError(Throwable unexpected, String threadName);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler}
     * ran into an unexpected problem, but without the stacktrace.
     *
     * @param threadName The name of the ComPort thread that ran into an unexpected problem
     * @param message The message of the unexpected problem
     */
    @Configuration(format = "Priority task scheduler ''{0}'' ran into the following unexpected problem: {1}", logLevel = LogLevel.ERROR)
    void unexpectedError(String threadName, String message);

    /**
     * Logs that the specified {@link HighPriorityTaskScheduler}
     * is interrupting ongoing work for the execution of the
     * specified {@link PriorityComTaskExecutionLink}.
     *
     * @param threadName The name of the ComPort thread that is interrupting ongoing work
     * @param priorityComTaskExecutionLink
     */
//    @Configuration(format = "Priority task scheduler ''{0}'' is interrupting ongoing work for the execution of: {1}", logLevel = LogLevel.ERROR)
//    void interruptingWork(String threadName, PriorityComTaskExecutionLink priorityComTaskExecutionLink);
}
