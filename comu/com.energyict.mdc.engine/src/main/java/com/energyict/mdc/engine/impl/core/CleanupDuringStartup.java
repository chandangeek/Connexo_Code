package com.energyict.mdc.engine.impl.core;

import java.sql.SQLException;

/**
 * Models the behavior that a {@link RunningComServer} needs to support
 * while starting up to cleanup after a forceful shutdown in the past.
 * A forceful shutdown is when the process was killed by the user
 * without allowing the process to properly shutdown and cleanup.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-01 (16:30)
 */
interface CleanupDuringStartup {

    /**
     * Cleans up any marker flags on ComTaskExecution
     * and/or ConnectionTasks that were not properly
     * cleaned because the process they were running on was actually forcefully shutdown,
     * i.e. not allowing it to shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits any ComServer from
     * picking up the tasks again because.
     *
     * @throws SQLException Indicates an unexpected database problem
     */
    void releaseInterruptedTasks() throws SQLException;

}