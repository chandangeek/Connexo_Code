package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.engine.impl.core.ServerProcess;

/**
 * Monitors long running tasks and will release them
 * when the execution time out has been exceeded.
 * Releasing a task means clearing both the
 * executing OutboundComPort
 * and the {@link com.energyict.mdc.tasks.ComTaskExecution#getExecutionStartedTimestamp()}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (14:33)
 */
public interface TimeOutMonitor extends ServerProcess {
}