package com.energyict.mdc.engine.impl.commands.store;

import java.time.Clock;
import java.time.Duration;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;

import com.elster.jupiter.util.time.StopWatch;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create an outbound ComSession.
 * Note that this task should be executed as late as possible
 * as the ComSession statistics include the execution time
 * of all DeviceCommands.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (11:41)
 */
public class CreateOutboundComSession extends ExecutionLoggerImpl implements CreateComSessionDeviceCommand {

    private final ScheduledConnectionTask connectionTask;
    private final ComSessionBuilder builder;
    private ComSession.SuccessIndicator successIndicator;
    private ComSession outboundComSession;
    private StopWatch stopWatch;

    public CreateOutboundComSession(ComServer.LogLevel communicationLogLevel, ScheduledConnectionTask connectionTask, ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator, Clock clock) {
        super(communicationLogLevel, clock);
        this.connectionTask = connectionTask;
        this.builder = builder;
        this.successIndicator = successIndicator;
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return connectionTask;
    }

    @Override
    public void setStopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public void updateSuccessIndicator(ComSession.SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    @Override
    public ComSessionBuilder getComSessionBuilder() {
        return builder;
    }

    @Override
    public void execute (ComServerDAO comServerDAO) {
        try {
            this.stopWatch.stop();
            this.builder.storeDuration(Duration.ofNanos(this.stopWatch.getElapsed()));
            this.outboundComSession = comServerDAO.createComSession(this.builder, successIndicator);
        }
        catch (RuntimeException e) {
            LoggerFactory.getLoggerFor(DeviceCommandLogger.class).outboundComSessionCreationFailed(e, this.connectionTask);
            throw e;
        }
    }

    @Override
    public ComSession getComSession() {
        return this.outboundComSession;
    }

    @Override
    public void executeDuringShutdown (ComServerDAO comServerDAO) {
        this.execute(comServerDAO);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.TRACE;
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // I am the ExecutionLogger so ignore this
    }

    @Override
    public String getDescriptionTitle() {
        return "Create outbound comSession command";
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        if (this.outboundComSession == null) {
            return "";
        }
        else {
            DescriptionBuilder builder = new DescriptionBuilderImpl(this);
            if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
                builder.addProperty("indicator").append(this.outboundComSession.getSuccessIndicator());
                builder.addProperty("connectionTaskId").append(this.outboundComSession.getConnectionTask().getId());
                builder.addProperty("comPortId").append(this.outboundComSession.getComPort().getId());
                builder.addProperty("number of tasks").append(this.outboundComSession.getComTaskExecutionSessions().size());
                builder.addProperty("number of journal entries").append(this.outboundComSession.getJournalEntries().size());
            }
            return builder.toString();
        }
    }

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel   The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(ComServer.LogLevel serverLogLevel, ComServer.LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }

}