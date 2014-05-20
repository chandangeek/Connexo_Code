package com.energyict.mdc.engine.impl.core.aspects.performance;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.PerformanceLogger;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Defines pointcuts and advice to monitor the performance of the {@link ComCommand}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-13 (14:24)
 */
public aspect ComCommandPerformance {

    private pointcut executeCommand (ComCommand command, DeviceProtocol deviceProtocol, ExecutionContext executionContext):
           execution(public void execute (DeviceProtocol, JobExecution.ExecutionContext))
        && target(command)
        && args(deviceProtocol, executionContext);

    void around (ComCommand command, DeviceProtocol deviceProtocol, ExecutionContext executionContext): executeCommand(command, deviceProtocol, executionContext) {
        LoggingStopWatch stopWatch = new LoggingStopWatch(command.getClass().getName() + ".execute", PerformanceLogger.INSTANCE);
        proceed(command, deviceProtocol, executionContext);
        stopWatch.stop();
    }

}