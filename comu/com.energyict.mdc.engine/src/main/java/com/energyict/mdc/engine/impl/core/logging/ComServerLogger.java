/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.sql.SQLException;

/**
 * Defines all the log messages for the {@link ComServer}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (08:47)
 */
public interface ComServerLogger {

    /**
     * Logs that the specified {@link ComServer} has started.
     *
     * @param comServer The ComServer
     */
    @Configuration(format = "Started ComServer {0} ...", logLevel = LogLevel.INFO)
    public void started(ComServer comServer);

    /**
     * Logs that a failure to start the specified {@link ComServer} occurred.
     *
     * @param comServer The ComServer
     * @param cause     The failure
     */
    @Configuration(format = "Failure to start ComServer {0} (see exception below)", logLevel = LogLevel.ERROR)
    public void startFailure(ComServer comServer, SQLException cause);

    /**
     * Logs that a failure to cleanup timed out {@link ComTaskExecution}s
     * that are running on the specified {@link ComServer} occurred.
     *
     * @param comServer The ComServer
     * @param cause     The failure
     */
    @Configuration(format = "Failure to clean up timedout tasks running on ComServer {0} (see exception below). Will postpone cleanup for 1 day.", logLevel = LogLevel.ERROR)
    public void timeOutCleanupFailure(ComServer comServer, DataAccessException cause);

    /**
     * Logs that a failure to cleanup outdated {@link ComTaskExecutionTrigger}s occurred.
     *
     * @param cause The failure
     */
    @Configuration(format = "Failure to clean up the outdated communication triggers on ComServer {0} (see exception below). Will postpone cleanup for 1 day.", logLevel = LogLevel.ERROR)
    public void comTaskExecutionTriggersCleanupFailure(ComServer comServer, DataAccessException cause);

    /**
     * Logs that the specified {@link ComServer} has started the shutdown process.
     *
     * @param comServer The ComServer
     */
    @Configuration(format = "Shutting down ComServer {0} ...", logLevel = LogLevel.INFO)
    public void shuttingDown(ComServer comServer);

    /**
     * Logs that the specified {@link ComServer} completed the shutdown process.
     *
     * @param comServer The ComServer
     */
    @Configuration(format = "ComServer {0} was shutdown completely", logLevel = LogLevel.INFO)
    public void shutDownComplete(ComServer comServer);

    /**
     * Logs that the specified {@link ComServer} is now monitoring for changes
     * applied to itself or to one of its {@link ComPort}s.
     *
     * @param comServer The ComServer
     */
    @Configuration(format = "Monitoring changes applied to ComServer {0}...", logLevel = LogLevel.DEBUG)
    public void monitoringChanges(ComServer comServer);

    /**
     * Logs that the specified {@link ComServer}'s attempt to monitor for changes
     * failed with the specified reason.
     *
     * @param comServer The ComServer
     */
    @Configuration(format = "Monitoring changes to ComServer {0} failed (see exception below)", logLevel = LogLevel.ERROR)
    public void monitorChangesFailed(ComServer comServer, Throwable e);

    /**
     * Logs that an OutboundComPort
     * was ignored during the startup process.
     *
     * @param comPortName The name of the OutboundComPort that was ignored during startup
     */
    @Configuration(format = "Ignored outbound comPort {0} because it is NOT active", logLevel = LogLevel.DEBUG)
    public void ignoredOutbound(String comPortName);

    /**
     * Logs that an InboundComPort
     * was ignored during the startup process.
     *
     * @param comPortName The name of the InboundComPort that was ignored during startup
     */
    @Configuration(format = "Ignored inbound comPort {0} because it is NOT active", logLevel = LogLevel.DEBUG)
    public void ignoredInbound(String comPortName);

    /**
     * Logs that changes were detected to OutboundComPorts
     * by the monitoring process.
     *
     * @param numberOfChanges The number of changes that were detected
     */
    @Configuration(format = "Detected {0} changed outbound ComPort(s) that will now be rescheduled", logLevel = LogLevel.DEBUG)
    public void outboundComPortChangesDetected(int numberOfChanges);

    /**
     * Logs that changes were detected to InboundComPort
     * by the monitoring process.
     *
     * @param numberOfChanges The number of changes that were detected
     */
    @Configuration(format = "Detected {0} changed inbound ComPort(s) that will now be rescheduled", logLevel = LogLevel.DEBUG)
    public void inboundComPortChangesDetected(int numberOfChanges);

    /**
     * Logs that the changes monitoring process detected that
     * OutboundComPorts were deactivated.
     *
     * @param numberOfDeactivatedComPorts The number of ComPorts that were deactivated
     */
    @Configuration(format = "Detected {0} deactivated outbound ComPort(s) that will now be unscheduled", logLevel = LogLevel.DEBUG)
    public void outboundComPortsDeactivated(int numberOfDeactivatedComPorts);

    /**
     * Logs that the changes monitoring process detected that
     * InboundComPort were deactivated.
     *
     * @param numberOfDeactivatedComPorts The number of ComPorts that were deactivated
     */
    @Configuration(format = "Detected {0} deactivated inbound ComPort(s) that will now be unscheduled", logLevel = LogLevel.INFO)
    public void inboundComPortsDeactivated(int numberOfDeactivatedComPorts);

    /**
     * Logs that new or recently activated .OutboundComPorts
     * were found by the changes monitoring process.
     *
     * @param numberOfNewComPorts The number of new ComPorts that were detected
     */
    @Configuration(format = "Detected {0} activated outbound ComPort(s) that will now be scheduled", logLevel = LogLevel.DEBUG)
    public void newOutboundComPortsDetected(int numberOfNewComPorts);

    /**
     * Logs that new or recently activated InboundComPorts
     * were found by the changes monitoring process.
     *
     * @param numberOfNewComPorts The number of new ComPorts that were detected
     */
    @Configuration(format = "Detected {0} activated inbound ComPort(s) that will now be scheduled", logLevel = LogLevel.DEBUG)
    public void newInboundComPortsDetected(int numberOfNewComPorts);

    /**
     * Logs the fact that a database exception occurred. Will postpone th monitor task and retry.
     *
     * @param comServer the ComServer which noticed the error
     * @param message   the database message
     */
    @Configuration(format = "DatabaseException occurred on ComServer {0}, will try to refresh ({1})", logLevel = LogLevel.ERROR)
    public void databaseErrorOccurredDuringUpdatingChanges(ComServer comServer, String message);

    /**
     * Logs the fact that we will try to restart the provided comport
     *
     * @param comPort the comport which we will try to restart
     */
    @Configuration(format = "Will try to restart comport {0}", logLevel = LogLevel.WARN)
    public void tryToRefreshComPortAfterDatabaseException(ComPort comPort);

    /**
     * Logs the fact that the restart succeeded
     *
     * @param comPort the comport which we will try to restart
     */
    @Configuration(format = "Successfully restarted comport {0}", logLevel = LogLevel.WARN)
    public void successfullyRestartedComport(ComPort comPort);

    /**
     * Logs the fact ComServer execution was interrupted.
     *
     * @param comServer
     * @param message
     */
    @Configuration(format = "ComServer {0}, execution interrupted: ({1})", logLevel = LogLevel.WARN)
    void comServerExecutionInterrupted(ComServer comServer, String message);

    /**
     * Logs any other exception which interrupted the comserver.
     *
     * @param comServer
     * @param message
     */
    @Configuration(format = "Unexpected exception occurred on ComServer {0}, will try to refresh ({1})", logLevel = LogLevel.ERROR)
    void otherComServerException(ComServer comServer, String message);

    /**
     * Logs the need to refresh all inbound and outbound ports of a comserver
     *
     * @param comServer
     */
    @Configuration(format = "Refreshing all OutBound and InBound ports from ComServer {0}", logLevel = LogLevel.WARN)
    void refreshingComServer(ComServer comServer);

    /**
     * Indicates that a com port was scheduled for refresh
     *
     * @param comPortName
     */
    @Configuration(format = "ComPort {0} will be refreshed asap", logLevel = LogLevel.INFO)
    void comPortScheduledForRefresh(String comPortName);
}