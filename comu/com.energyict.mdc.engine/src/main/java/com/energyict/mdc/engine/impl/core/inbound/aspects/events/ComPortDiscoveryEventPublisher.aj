package com.energyict.mdc.engine.impl.core.inbound.aspects.events;

import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.aspects.logging.AbstractComPortDiscoveryLogging;
import com.energyict.mdc.engine.impl.core.inbound.aspects.logging.ComPortDiscoveryLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will create
 * {@link com.energyict.comserver.events.LoggingEvent}s
 * inbound communication sessions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (10:27)
 */
public aspect ComPortDiscoveryEventPublisher extends AbstractComPortDiscoveryLogging {

    @Override
    protected ComPortDiscoveryLogger getUniqueLogger (InboundCommunicationHandler handler) {
        return this.getLogger(handler);
    }

    @Override
    protected Logger attachHandlerTo (ComPortDiscoveryLogger loggger, InboundDiscoveryContextImpl context) {
        // Handler was already attached at creation time
        return ((LoggerFactory.LoggerHolder) loggger).getLogger();
    }

    @Override
    protected ComPortDiscoveryLogger getLogger (InboundCommunicationHandler handler) {
        return LoggerFactory.getLoggerFor(ComPortDiscoveryLogger.class, this.getAnonymousLogger(handler));
    }

    private Logger getAnonymousLogger (InboundCommunicationHandler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ComPortDiscoveryLogHandler(handler));
        return logger;
    }

}