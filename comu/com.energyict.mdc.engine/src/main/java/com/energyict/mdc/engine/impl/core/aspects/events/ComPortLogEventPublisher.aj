package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.engine.impl.core.ComPortServerProcess;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.aspects.logging.AbstractComPortLogging;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComPortConnectionLogger;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComPortOperationsLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComPort;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will emit
 * {@link com.energyict.mdc.engine.events.LoggingEvent}s
 * for the {@link com.energyict.mdc.engine.impl.core.ComPortServerProcess} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (10:27)
 */
public aspect ComPortLogEventPublisher extends AbstractComPortLogging {

    private Logger getAnonymousLogger (ExecutionContext executionContext) {
        return this.getAnonymousLogger(new ComPortLogHandler(executionContext.getComPort()));
    }

    private Logger getAnonymousLogger (Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(handler);
        return logger;
    }

    @Override
    protected ComPortConnectionLogger initializeUniqueLogger (ComPort comPort, ExecutionContext executionContext, LogLevel logLevel) {
        return LoggerFactory.getLoggerFor(ComPortConnectionLogger.class, this.getAnonymousLogger(executionContext));
    }

    @Override
    protected ComPortOperationsLogger getOperationsLogger (ComPortServerProcess comPortProcess) {
        if (this.getEventOperationsLogger(comPortProcess) == null) {
            ComPort comPort = comPortProcess.getComPort();
            this.setEventOperationsLogger(comPortProcess, this.newOperationsLogger(comPort));
        }
        return this.getEventOperationsLogger(comPortProcess);
    }

    private ComPortOperationsLogger newOperationsLogger (ComPort comPort) {
        return LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, this.getAnonymousLogger(new ComPortLogHandler(comPort)));
    }

}