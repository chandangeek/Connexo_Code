package com.energyict.mdc.engine.impl.scheduling.aspects.logging;

import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.scheduling.RunningComServer;
import com.energyict.mdc.engine.impl.scheduling.RunningComServerImpl;
import com.energyict.mdc.engine.impl.scheduling.TimeOutMonitorImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundCapableComServer;

import java.util.Collection;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link RunningComServer} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (08:49)
 */
public aspect ComServerLogging {

    private pointcut starting (RunningComServer comServer):
            execution(void RunningComServerImpl.continueStartupAfterCleanup())
         && target(comServer);

    after (RunningComServer comServer): starting(comServer) {
        this.getLogger(comServer).started(comServer.getComServer());
    }

    private pointcut startFailure (RunningComServer comServer):
            execution(void RunningComServerImpl.cleanupFailed())
         && target(comServer);

    after (RunningComServer comServer): startFailure(comServer) {
        this.getLogger(comServer).started(comServer.getComServer());
    }

    private pointcut shuttingDown (RunningComServer comServer):
            execution(void RunningComServer.shutdown())
         && target(comServer);

    before (RunningComServer comServer): shuttingDown(comServer) {
        this.getLogger(comServer).shuttingDown(comServer.getComServer());
    }

    private pointcut monitorChanges (RunningComServerImpl comServer):
            execution(private void RunningComServerImpl.monitorChanges())
         && target(comServer);

    before (RunningComServerImpl comServer): monitorChanges(comServer) {
        this.getLogger(comServer).monitoringChanges(comServer.getComServer());
    }

    private pointcut ignoreOutbound (OutboundComPort comPort):
            execution(private void RunningComServerImpl.ignored(OutboundComPort))
         && args(comPort);

    after (OutboundComPort comPort): ignoreOutbound(comPort) {
        this.getLogger(comPort.getComServer()).ignoredOutbound(comPort.getName());
    }

    private pointcut ignoreInbound (InboundComPort comPort):
            execution(private void RunningComServerImpl.ignored(InboundComPort))
         && args(comPort);

    after (InboundComPort comPort): ignoreInbound(comPort) {
        this.getLogger(comPort.getComServer()).ignoredInbound(comPort.getName());
    }

    private pointcut outboundComPortChangesDetected (InboundCapableComServer newVersion):
            execution(private Collection<OutboundComPort> RunningComServerImpl.changedOutboundComPortsIn(*))
         && args(newVersion);

    Collection<OutboundComPort> around (InboundCapableComServer newVersion): outboundComPortChangesDetected(newVersion) {
        Collection<OutboundComPort> changes = proceed(newVersion);
        this.getLogger(newVersion).outboundComPortChangesDetected(changes.size());
        return changes;
    }

    private pointcut inboundComPortChangesDetected (InboundCapableComServer newVersion):
            execution(private Collection<InboundComPort> RunningComServerImpl.changedInboundComPortsIn(*))
         && args(newVersion);

    Collection<InboundComPort> around (InboundCapableComServer newVersion): inboundComPortChangesDetected(newVersion) {
        Collection<InboundComPort> changes = proceed(newVersion);
        this.getLogger(newVersion).inboundComPortChangesDetected(changes.size());
        return changes;
    }

    private pointcut newActivatedOutboundComPortsDetected (InboundCapableComServer newVersion):
            execution(private Collection<OutboundComPort> RunningComServerImpl.newActivatedOutboundComPortsIn(*))
         && args(newVersion);

    Collection<OutboundComPort> around (InboundCapableComServer newVersion): newActivatedOutboundComPortsDetected(newVersion) {
        Collection<OutboundComPort> newlyActivated = proceed(newVersion);
        this.getLogger(newVersion).newOutboundComPortsDetected(newlyActivated.size());
        return newlyActivated;
    }

    private pointcut newActivatedInboundComPortsDetected (InboundCapableComServer newVersion):
            execution(private Collection<InboundComPort> RunningComServerImpl.newActivatedInboundComPortsIn(*))
         && args(newVersion);

    Collection<InboundComPort> around (InboundCapableComServer newVersion): newActivatedInboundComPortsDetected(newVersion) {
        Collection<InboundComPort> newlyActivated = proceed(newVersion);
        this.getLogger(newVersion).newInboundComPortsDetected(newlyActivated.size());
        return newlyActivated;
    }

    private pointcut deactivatedOutboundComPortsDetected (InboundCapableComServer newVersion):
            execution(private Collection<OutboundComPort> RunningComServerImpl.deactivatedOutboundComPortsIn(*))
         && args(newVersion);

    Collection<OutboundComPort> around (InboundCapableComServer newVersion): deactivatedOutboundComPortsDetected(newVersion) {
        Collection<OutboundComPort> deactivated = proceed(newVersion);
        this.getLogger(newVersion).outboundComPortsDeactivated(deactivated.size());
        return deactivated;
    }

    private pointcut deactivatedInboundComPortsDetected (InboundCapableComServer newVersion):
            execution(private Collection<InboundComPort> RunningComServerImpl.deactivatedInboundComPortsIn(*))
         && args(newVersion);

    Collection<InboundComPort> around (InboundCapableComServer newVersion): deactivatedInboundComPortsDetected(newVersion) {
        Collection<InboundComPort> deactivated = proceed(newVersion);
        this.getLogger(newVersion).inboundComPortsDeactivated(deactivated.size());
        return deactivated;
    }

    private pointcut cleanupFailure (TimeOutMonitorImpl monitor):
            call(long TimeOutMonitorImpl.releaseTimedOutTasks())
         && target(monitor);

    after (TimeOutMonitorImpl monitor) throwing (DataAccessException e) : cleanupFailure(monitor) {
        OutboundCapableComServer comServer = monitor.getComServer();
        this.getLogger(comServer).timeOutCleanupFailure(comServer, e);
    }

    private ComServerLogger getLogger (RunningComServer comServer) {
        return this.getLogger(comServer.getComServer());
    }

    private ComServerLogger getLogger (ComServer comServer) {
        return LoggerFactory.getLoggerFor(ComServerLogger.class, this.getServerLogLevel(comServer));
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

}