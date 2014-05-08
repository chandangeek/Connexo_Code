package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.core.ComChannelBasedComPortListenerImpl;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComPortListenerImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link ComPortListenerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:37)
 */
public aspect InboundComPortLogging {
    declare precedence : InboundComPortLogging, com.energyict.comserver.scheduling.aspects.logging.ComPortLogging;

    private pointcut started (ComPortListenerImpl comPort):
            execution(void ComPortListenerImpl.start())
                && target(comPort);

    after (ComPortListenerImpl comPort): started(comPort) {
        this.getLogger(comPort).started(comPort.getThreadName());
    }

    private pointcut shuttingDown (ComPortListenerImpl comPort):
            execution(void ComPortListenerImpl.shutdown())
                && target(comPort);

    before (ComPortListenerImpl comPort): shuttingDown(comPort) {
        this.getLogger(comPort).shuttingDown(comPort.getThreadName());
    }

    private pointcut monitorChanges (ComPortListenerImpl comPort):
            execution(public void ComPortListenerImpl.checkAndApplyChanges())
                && target(comPort);

    before (ComPortListenerImpl comPort): monitorChanges(comPort) {
        this.getLogger(comPort).monitoringChanges(comPort.getComPort());
    }

    private pointcut listen (ComChannelBasedComPortListenerImpl comPort):
            execution(protected * ComChannelBasedComPortListenerImpl.listen())
                && target(comPort);

    before (ComChannelBasedComPortListenerImpl comPort): listen(comPort) {
        this.getLogger(comPort).listening(comPort.getThreadName());
    }

    private InboundComPortLogger getLogger (ComPortListener comPort) {
        return this.getLogger(comPort.getComPort());
    }

    private InboundComPortLogger getLogger (ComPort comPort) {
        return LoggerFactory.getLoggerFor(InboundComPortLogger.class, this.getServerLogLevel(comPort));
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comPort) {
        return LogLevelMapper.map(comPort.getServerLogLevel());
    }

}