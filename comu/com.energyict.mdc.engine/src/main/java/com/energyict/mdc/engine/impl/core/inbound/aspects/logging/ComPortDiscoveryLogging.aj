package com.energyict.mdc.engine.impl.core.inbound.aspects.logging;

import com.energyict.comserver.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContextImpl;

import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for
 * inbound communication sessions.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-24 (11:49)
 */
public aspect ComPortDiscoveryLogging extends AbstractComPortDiscoveryLogging {

    protected ComPortDiscoveryLogger getUniqueLogger (InboundCommunicationHandler handler) {
        return LoggerFactory.getUniqueLoggerFor(ComPortDiscoveryLogger.class, this.getServerLogLevel(handler));
    }

    protected Logger attachHandlerTo (ComPortDiscoveryLogger loggger, InboundDiscoveryContextImpl context) {
        Logger actualLogger = ((LoggerFactory.LoggerHolder) loggger).getLogger();
        actualLogger.addHandler(new DiscoveryContextLogHandler(context));
        context.setLogger(actualLogger);
        return actualLogger;
    }

    protected ComPortDiscoveryLogger getLogger (InboundCommunicationHandler handler) {
        return LoggerFactory.getLoggerFor(ComPortDiscoveryLogger.class, handler.getContext().getLogger());
    }

}