package com.energyict.mdc.engine.impl.events.aspects;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.devices.AbstractDeviceCommandExecutorLogging;
import com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorLogger;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will emit {@link com.energyict.mdc.engine.events.LoggingEvent}s
 * for the {@link DeviceCommandExecutor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:01)
 */
public aspect DeviceCommandExecutorLogEventPublisher extends AbstractDeviceCommandExecutorLogging {
    declare precedence:
            com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorLogging,
            DeviceCommandExecutorLogEventPublisher;

    @Override
    protected DeviceCommandExecutorLogger getLogger (DeviceCommandExecutor deviceCommandExecutor) {
        return LoggerFactory.getLoggerFor(DeviceCommandExecutorLogger.class, this.getAnonymousLogger());
    }

    private Logger getAnonymousLogger () {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new DeviceCommandExecutorLogHandler());
        return logger;
    }

}