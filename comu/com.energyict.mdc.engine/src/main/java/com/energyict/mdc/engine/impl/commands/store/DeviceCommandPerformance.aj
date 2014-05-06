package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.logging.PerformanceLogger;

/**
 * Defines pointcuts and advice to monitor the performance of the {@link DeviceCommand}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-13 (14:50)
 */
public aspect DeviceCommandPerformance {

    private pointcut executeCommand (DeviceCommand command, ComServerDAO comServerDAO):
           execution(public void execute (ComServerDAO))
        && target(command)
        && args(comServerDAO);

    void around (DeviceCommand command, ComServerDAO comServerDAO): executeCommand(command, comServerDAO) {
        LoggingStopWatch stopWatch = new LoggingStopWatch(command.getClass().getName() + ".execute", PerformanceLogger.INSTANCE);
        proceed(command, comServerDAO);
        stopWatch.stop();
    }

}