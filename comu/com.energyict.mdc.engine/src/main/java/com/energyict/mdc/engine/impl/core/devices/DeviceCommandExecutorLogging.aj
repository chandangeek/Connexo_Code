package com.energyict.mdc.engine.impl.core.devices;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link DeviceCommandExecutor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-27 (09:07)
 */
public aspect DeviceCommandExecutorLogging extends AbstractDeviceCommandExecutorLogging {

    protected DeviceCommandExecutorLogger getLogger (DeviceCommandExecutor deviceCommandExecutor) {
        return LoggerFactory.getLoggerFor(DeviceCommandExecutorLogger.class, this.getLogLevel(deviceCommandExecutor.getLogLevel()));
    }

    private LogLevel getLogLevel (ComServer.LogLevel logLevel) {
        return LogLevelMapper.map(logLevel);
    }

}