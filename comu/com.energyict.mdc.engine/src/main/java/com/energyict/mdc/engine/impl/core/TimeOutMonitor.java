/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTaskExecution;

/**
 * Monitors long running tasks and will release them
 * when the execution time out has been exceeded.
 * Releasing a task means clearing both the
 * executing OutboundComPort
 * and the {@link ComTaskExecution#getExecutionStartedTimestamp()}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (14:33)
 */
interface TimeOutMonitor extends ServerProcess {
}